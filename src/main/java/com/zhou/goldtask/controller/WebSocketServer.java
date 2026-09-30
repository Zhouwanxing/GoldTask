package com.zhou.goldtask.controller;

import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.WsData;
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
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {
    /** 心跳会刷新 idle 计时;超过该时长没有任何消息判定死连接。 */
    private static final int MAX_IDLE_MS = 90_000;
    /** sid -> 会话信息。多线程读写,必须 ConcurrentHashMap。 */
    private static final Map<String, ClientInfo> SESSION_MAP = new ConcurrentHashMap<>();

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
        // 应用层心跳:直接应答,不转发。
        if ("ping".equals(wsData.getType())) {
            sendToClient(sid, WsData.builder().type("pong").from("system").build());
            return;
        }
        log.info("收到来自客户端: {} 的信息: {}", sid, message);
        wsData.setFrom(sid);
        wsData.setType("newMessage");
        wsData.setId(System.currentTimeMillis() + "");
        sendToClient(wsData.getTo(), wsData);
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

    private void remove(String sid, String reason) {
        ClientInfo info = SESSION_MAP.remove(sid);
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

    private void sendOnlineUser() {
        sendToAllClient(WsData.builder().type("onlineUser").from("system")
                .message(String.join(",", SESSION_MAP.keySet())).build());
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

    /** 同一会话串行发送,避免多线程并发写帧。 */
    private void send(Session session, String message) {
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
