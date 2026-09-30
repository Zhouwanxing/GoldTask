package com.zhou.goldtask.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 房间生命周期管理。所有方法 synchronized 串行处理,内部用 ConcurrentHashMap 只是顺带。
 * 不当 Spring bean:JSR-356 端点是每连接一个实例,由 WebSocketServer 以 static 单例持有。
 */
public class RoomManager {
    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sidRoom = new ConcurrentHashMap<>();

    public enum JoinResult { OK, NOT_FOUND, FULL, BUSY }

    /** 创建等待房,返回 4 位数字码;已在别的房返回 null。 */
    public synchronized String createRoom(String hostSid) {
        if (sidRoom.containsKey(hostSid)) {
            return null;
        }
        String id = nextId();
        rooms.put(id, new GameRoom(id, hostSid));
        sidRoom.put(hostSid, id);
        return id;
    }

    public synchronized JoinResult joinRoom(String sid, String roomId) {
        if (sidRoom.containsKey(sid)) {
            return JoinResult.BUSY;
        }
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            return JoinResult.NOT_FOUND;
        }
        if (room.getGuestSid() != null) {
            return JoinResult.FULL;
        }
        room.setGuestSid(sid);
        sidRoom.put(sid, roomId);
        return JoinResult.OK;
    }

    /** 邀请接受后直接成房(挑战方=房主)。任一方已在房返回 null。 */
    public synchronized String createRoomWith(String hostSid, String guestSid) {
        if (sidRoom.containsKey(hostSid) || sidRoom.containsKey(guestSid)) {
            return null;
        }
        String id = nextId();
        GameRoom room = new GameRoom(id, hostSid);
        room.setGuestSid(guestSid);
        rooms.put(id, room);
        sidRoom.put(hostSid, id);
        sidRoom.put(guestSid, id);
        return id;
    }

    /** 主动离开:删房,返回对方 sid(没有则 null)。 */
    public synchronized String leaveRoom(String sid) {
        String roomId = sidRoom.remove(sid);
        if (roomId == null) {
            return null;
        }
        GameRoom room = rooms.remove(roomId);
        if (room == null) {
            return null;
        }
        String opponent = room.opponentOf(sid);
        if (opponent != null) {
            sidRoom.remove(opponent);
        }
        return opponent;
    }

    /**
     * 断线:标记离线。双方均离线则删房返回 null;否则返回对方 sid(调用方视其在线与否发 opponentOffline)。
     */
    public synchronized String markOffline(String sid) {
        String roomId = sidRoom.get(sid);
        if (roomId == null) {
            return null;
        }
        GameRoom room = rooms.get(roomId);
        if (room == null) {
            sidRoom.remove(sid);
            return null;
        }
        room.setOnline(sid, false);
        if (room.bothOffline()) {
            rooms.remove(roomId);
            sidRoom.remove(room.getHostSid());
            if (room.getGuestSid() != null) {
                sidRoom.remove(room.getGuestSid());
            }
            return null;
        }
        return room.opponentOf(sid);
    }

    /** 重连回房:房间在且该 sid 是成员则标记在线并返回房间;否则 null。 */
    public synchronized GameRoom rejoin(String sid, String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null || !room.hasSid(sid)) {
            return null;
        }
        room.setOnline(sid, true);
        sidRoom.put(sid, roomId);
        return room;
    }

    public synchronized GameRoom roomOf(String sid) {
        String roomId = sidRoom.get(sid);
        return roomId == null ? null : rooms.get(roomId);
    }

    private String nextId() {
        for (int i = 0; i < 200; i++) {
            String id = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
            if (!rooms.containsKey(id)) {
                return id;
            }
        }
        throw new IllegalStateException("房间码耗尽");
    }
}
