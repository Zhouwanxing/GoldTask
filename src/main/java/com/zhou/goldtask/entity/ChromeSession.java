package com.zhou.goldtask.entity;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Vector;

@Slf4j
public class ChromeSession {
    private final String devId;
    private final Vector<JSONObject> states = new Vector<>();
    private final Object pollingLock = new Object();

    public ChromeSession(String devId) {
        this.devId = devId;
    }

    public JSONObject parsePacket() {
        JSONObject res = new JSONObject();
        if (states.size() > 0) {
            res.putOpt("Event", states.remove(0));
            return res;
        }
        synchronized (pollingLock) {
            try {
                pollingLock.wait(5000);
            } catch (InterruptedException e) {
                log.warn("{}", devId, e);
            }
        }
        if (states.size() > 0) {
            res.putOpt("Event", states.remove(0));
            return res;
        }
        return res;
    }

    public void notifyEvent(JSONObject event) {
        states.add(event);
        synchronized (pollingLock) {
            pollingLock.notifyAll();
        }
    }
}