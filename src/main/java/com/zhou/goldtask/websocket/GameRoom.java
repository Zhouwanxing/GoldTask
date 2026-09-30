package com.zhou.goldtask.websocket;

import lombok.Data;

/**
 * 跳棋房间。房主执红(bottom)先手,客人执蓝(top)。
 * guestSid 为 null 表示房间已创建还没人加入。
 */
@Data
public class GameRoom {
    private final String id;
    private final String hostSid;
    private String guestSid;
    private boolean hostOnline = true;
    private boolean guestOnline = true;
    private final long createdAt = System.currentTimeMillis();

    public GameRoom(String id, String hostSid) {
        this.id = id;
        this.hostSid = hostSid;
    }

    public boolean hasSid(String sid) {
        return sid.equals(hostSid) || sid.equals(guestSid);
    }

    /** 对方 sid;客人还没进房时为 null。 */
    public String opponentOf(String sid) {
        return sid.equals(hostSid) ? guestSid : hostSid;
    }

    public boolean isHost(String sid) {
        return sid.equals(hostSid);
    }

    public void setOnline(String sid, boolean online) {
        if (isHost(sid)) {
            hostOnline = online;
        } else {
            guestOnline = online;
        }
    }

    public boolean isOnline(String sid) {
        return isHost(sid) ? hostOnline : guestOnline;
    }

    /** 双方都不在(含客人从未进房)则房间没用了。 */
    public boolean bothOffline() {
        return !hostOnline && (guestSid == null || !guestOnline);
    }
}
