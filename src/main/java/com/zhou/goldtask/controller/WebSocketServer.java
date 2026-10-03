package com.zhou.goldtask.controller;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.WsData;
import com.zhou.goldtask.service.MarbleBankService;
import com.zhou.goldtask.websocket.GameRoom;
import com.zhou.goldtask.websocket.RoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务。
 * 端点:/ws/{sid}?name={设备名}
 * sid = 客户端设备唯一标识(UUID);设备名走 query,容器自动解码,带中文/特殊字符都安全。
 * 心跳:客户端每 25s 发 {"type":"ping"},这里回 {"type":"pong"};90s 无消息判定死连接自动清理。
 * 游戏:按 type 分发——房间操作(createRoom/joinRoom/leaveRoom/rejoinRoom/acceptInvite)由服务端处理,
 * 其余(invite/gameMove/surrender 等)保留 type 点对点转发。
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {
    /** 心跳会刷新 idle 计时;超过该时长没有任何消息判定死连接。 */
    private static final int MAX_IDLE_MS = 90_000;
    /** sid -> 会话信息。多线程读写,必须 ConcurrentHashMap。 */
    private static final Map<String, ClientInfo> SESSION_MAP = new ConcurrentHashMap<>();
    /** 房间管理。static 单例:端点每连接一个实例,状态必须共享。 */
    private static final RoomManager ROOM_MANAGER = new RoomManager();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        session.setMaxIdleTimeout(MAX_IDLE_MS);
        String name = deviceName(session);
        SESSION_MAP.put(sid, new ClientInfo(sid, name, session));
        log.info("客户端上线: sid={}, name={}, 当前在线={}", sid, name, SESSION_MAP.size());
        sendOnlineUser();
        try {
            SpringUtil.getBean(MarbleBankService.class).touch(sid, name);
        } catch (Exception e) {
            log.warn("登记弹珠设备失败: sid={}, {}", sid, e.toString());
        }
        pushTo(sid, WsData.builder().type("marbleQuery").from("system").message("").build());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        if (!JSONUtil.isTypeJSON(message)) {
            log.warn("收到非 JSON 消息,忽略: sid={}, message={}", sid, message);
            return;
        }
        WsData wsData = JSONUtil.toBean(message, WsData.class);
        String type = wsData.getType();
        if (type == null) {
            return;
        }
        switch (type) {
            case "ping":
                sendToClient(sid, WsData.builder().type("pong").from("system").build());
                return;
            case "createRoom":
                onCreateRoom(sid);
                return;
            case "joinRoom":
                onJoinRoom(sid, wsData.getMessage());
                return;
            case "leaveRoom":
                onLeaveRoom(sid);
                return;
            case "rejoinRoom":
                onRejoin(sid, wsData.getMessage());
                return;
            case "acceptInvite":
                onAcceptInvite(sid, wsData.getTo());
                return;
            default:
                relay(sid, wsData);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        remove(sid, "连接关闭");
    }

    /**
     * 连接异常调用的方法:清理会话,避免脏连接占着在线列表。
     */
    @OnError
    public void onError(@PathParam("sid") String sid, Throwable error) {
        log.warn("连接异常: sid={}, {}", sid, error.toString());
        remove(sid, "异常");
    }

    // MARK: - 房间操作(服务端处理型)

    private void onCreateRoom(String sid) {
        String roomId = ROOM_MANAGER.createRoom(sid);
        if (roomId == null) {
            sendToClient(sid, WsData.builder().type("roomJoinFailed").from("system").message("busy").build());
            return;
        }
        log.info("房间创建: roomId={}, host={}", roomId, sid);
        sendToClient(sid, WsData.builder().type("roomCreated").from("system").message(roomId).build());
    }

    private void onJoinRoom(String sid, String roomId) {
        RoomManager.JoinResult result = roomId == null
                ? RoomManager.JoinResult.NOT_FOUND
                : ROOM_MANAGER.joinRoom(sid, roomId.trim());
        if (result != RoomManager.JoinResult.OK) {
            String reason = result == RoomManager.JoinResult.NOT_FOUND ? "房间不存在"
                    : result == RoomManager.JoinResult.FULL ? "房间已满" : "busy";
            sendToClient(sid, WsData.builder().type("roomJoinFailed").from("system").message(reason).build());
            return;
        }
        GameRoom room = ROOM_MANAGER.roomOf(sid);
        log.info("房间加入: roomId={}, guest={}", roomId, sid);
        notifyGameStart(room);
    }

    private void onAcceptInvite(String sid, String challengerSid) {
        if (challengerSid == null) {
            return;
        }
        String roomId = ROOM_MANAGER.createRoomWith(challengerSid, sid);
        if (roomId == null) {
            sendToClient(sid, WsData.builder().type("roomJoinFailed").from("system").message("busy").build());
            return;
        }
        log.info("邀请成局: roomId={}, host={}, guest={}", roomId, challengerSid, sid);
        notifyGameStart(ROOM_MANAGER.roomOf(sid));
    }

    /** 双方各收一份 gameStart,side 指明红蓝,opponentName 用于界面显示。 */
    private void notifyGameStart(GameRoom room) {
        sendToClient(room.getHostSid(), roomPayload("gameStart", room, room.getHostSid()));
        sendToClient(room.getGuestSid(), roomPayload("gameStart", room, room.getGuestSid()));
    }

    private WsData roomPayload(String type, GameRoom room, String sid) {
        String opponent = room.opponentOf(sid);
        JSONObject payload = new JSONObject();
        payload.set("roomId", room.getId());
        payload.set("side", room.isHost(sid) ? "red" : "blue");
        payload.set("opponent", opponent);
        ClientInfo opp = opponent == null ? null : SESSION_MAP.get(opponent);
        payload.set("opponentName", opp == null ? "未知设备" : opp.deviceName);
        return WsData.builder().type(type).from("system").message(payload.toString()).build();
    }

    private void onLeaveRoom(String sid) {
        String opponent = ROOM_MANAGER.leaveRoom(sid);
        log.info("离开房间: sid={}, 通知对方={}", sid, opponent);
        if (opponent != null) {
            sendToClient(opponent, WsData.builder().type("opponentLeft").from("system").build());
        }
    }

    private void onRejoin(String sid, String roomId) {
        GameRoom room = roomId == null ? null : ROOM_MANAGER.rejoin(sid, roomId.trim());
        if (room == null) {
            sendToClient(sid, WsData.builder().type("rejoinFailed").from("system").build());
            return;
        }
        log.info("重连回房: roomId={}, sid={}", room.getId(), sid);
        sendToClient(sid, roomPayload("rejoinOk", room, sid));
        String opponent = room.opponentOf(sid);
        if (opponent != null && room.isOnline(opponent)) {
            sendToClient(opponent, WsData.builder().type("opponentBack").from("system").build());
        }
    }

    /** 转发型消息:保留 type,补 from/id;invite 附带发起方设备名;目标不在线回 messageFailed。 */
    private void relay(String sid, WsData wsData) {
        wsData.setFrom(sid);
        wsData.setId(System.currentTimeMillis() + "");
        if ("invite".equals(wsData.getType())) {
            ClientInfo from = SESSION_MAP.get(sid);
            JSONObject payload = new JSONObject();
            payload.set("fromName", from == null ? "未知设备" : from.deviceName);
            wsData.setMessage(payload.toString());
        }
        String to = wsData.getTo();
        ClientInfo target = to == null ? null : SESSION_MAP.get(to);
        if (target == null || !target.session.isOpen()) {
            sendToClient(sid, WsData.builder().type("messageFailed").from("system").message(wsData.getType()).build());
            return;
        }
        log.info("转发: {} -> {}, type={}", sid, to, wsData.getType());
        sendToClient(to, wsData);
    }

    // MARK: - 连接生命周期

    private void remove(String sid, String reason) {
        ClientInfo info = SESSION_MAP.remove(sid);
        String opponent = ROOM_MANAGER.markOffline(sid);
        if (opponent != null) {
            // 对方还在线才通知;双方都离线时房间已删,opponent 为 null
            sendToClient(opponent, WsData.builder().type("opponentOffline").from("system").build());
        }
        if (info != null) {
            log.info("客户端下线({}): sid={}, name={}, 当前在线={}", reason, sid, info.deviceName, SESSION_MAP.size());
            sendOnlineUser();
        }
    }

    /** 设备名从 query 参数 name 取,容器已解码;缺省「未知设备」。 */
    private String deviceName(Session session) {
        Map<String, List<String>> params = session.getRequestParameterMap();
        List<String> names = params.get("name");
        if (names == null || names.isEmpty() || names.get(0) == null || names.get(0).isEmpty()) {
            return "未知设备";
        }
        return names.get(0);
    }

    /** 在线列表广播:[{sid, name}] JSON 数组,大厅要显示设备名。 */
    private void sendOnlineUser() {
        JSONArray arr = new JSONArray();
        for (ClientInfo info : SESSION_MAP.values()) {
            JSONObject item = new JSONObject();
            item.set("sid", info.sid);
            item.set("name", info.deviceName);
            arr.add(item);
        }
        sendToAllClient(WsData.builder().type("onlineUser").from("system").message(arr.toString()).build());
    }

    /**
     * 群发
     */
    public void sendToAllClient(WsData message) {
        sendToAllClient(message.toString());
    }

    public void sendToAllClient(String message) {
        for (ClientInfo info : SESSION_MAP.values()) {
            send(info.session, message);
        }
    }

    public void sendToClient(String sid, String message) {
        if (sid == null) {
            return;
        }
        ClientInfo info = SESSION_MAP.get(sid);
        if (info != null) {
            send(info.session, message);
        }
    }

    public void sendToClient(String sid, WsData message) {
        sendToClient(sid, message.toString());
    }

    public static boolean isOnline(String sid) {
        ClientInfo info = sid == null ? null : SESSION_MAP.get(sid);
        return info != null && info.session != null && info.session.isOpen();
    }

    /** 库存已确定时下发绝对数量。 */
    public static void pushMarbleBank(String sid, int count) {
        pushTo(sid, WsData.builder().type("marbleBank").from("system").message(Integer.toString(count)).build());
    }

    /** 库存还没上报时，只下发这次加上的数量。 */
    public static void pushMarbleDelta(String sid, int delta) {
        pushTo(sid, WsData.builder().type("marbleDelta").from("system").message(Integer.toString(delta)).build());
    }

    private static void pushTo(String sid, WsData data) {
        ClientInfo info = sid == null ? null : SESSION_MAP.get(sid);
        if (info == null || info.session == null || !info.session.isOpen()) {
            return;
        }
        send(info.session, data.toString());
    }

    /** 同一会话串行发送,避免多线程并发写帧。 */
    private static void send(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.warn("发送失败: {}", e.toString());
        }
    }

    /** 一条连接的全部上下文。后续多人游戏按 sid / deviceName 找人。 */
    private static final class ClientInfo {
        private final String sid;
        private final String deviceName;
        private final Session session;

        private ClientInfo(String sid, String deviceName, Session session) {
            this.sid = sid;
            this.deviceName = deviceName;
            this.session = session;
        }
    }
}
