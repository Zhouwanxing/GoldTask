package com.zhou.goldtask.service;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.ChromeSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

@Service
@Slf4j
public class ChromeService {
    private final Hashtable<String, ChromeSession> chromeSessions = new Hashtable<>();

    public JSONObject getState(String devId) {
        ChromeSession session = null;
        synchronized (chromeSessions) {
            session = chromeSessions.get(devId);
            if (session == null) {
                session = new ChromeSession(devId);
                this.addSession(devId, session);
            }
        }
        return session.parsePacket();
    }

    public void addSession(String devId, ChromeSession chromeSession) {
        chromeSessions.put(devId, chromeSession);
    }

    public void toChromeCode(String devId, String code) {
        ChromeSession chromeSession = chromeSessions.get(devId);
        if (chromeSession != null) {
            chromeSession.notifyEvent(new JSONObject().set("type", "codeToHtml").set("code", code));
        }
    }
}