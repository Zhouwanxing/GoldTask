package com.zhou.goldtask.websocket;

import lombok.Data;

/**
 * 对战房间。房主执红先手,客人执蓝。
 * game 为 checkers 或 pool,两种玩法不能进同一个房间。
 * guestSid 为 null 表示房间已创建还没人加入。
 */
@Data
public class GameRoom {
    private final String id;
    private final String hostSid;
    /** checkers 或 pool。 */
    private final String game;
    private String guestSid;
    private boolean hostOnline = true;
    private boolean guestOnline = true;
    private final long createdAt = System.currentTimeMillis();

    public GameRoom(String id, String hostSid, String game) {
        this.id = id;
        this.hostSid = hostSid;
        this.game = game == null || game.trim().isEmpty() ? "checkers" : game;
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
