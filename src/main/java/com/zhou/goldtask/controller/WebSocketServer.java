package com.zhou.goldtask.controller;

import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.WsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {
    //存放会话对象
    private static final Map<String, Session> sessionMap = new HashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        log.info("客户端：" + sid + "建立连接");
        sessionMap.put(sid, session);
        sendOnlineUser();
    }

    private void sendOnlineUser() {
        sendToAllClient(WsData.builder().type("onlineUser").from("system").message(String.join(",", sessionMap.keySet())).build());

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("收到来自客户端：" + sid + "的信息:" + message);
        if (JSONUtil.isTypeJSON(message)) {
            WsData wsData = JSONUtil.toBean(message, WsData.class);
            wsData.setFrom(sid);
            wsData.setType("newMessage");
            wsData.setId(System.currentTimeMillis() + "");
            sendToClient(wsData.getTo(), wsData);
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid 1
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        log.info("连接断开:" + sid);
        sessionMap.remove(sid);
        sendOnlineUser();
    }

    /**
     * 群发
     *
     * @param message 1
     */
    public void sendToAllClient(WsData message) {
        sendToAllClient(message.toString());
    }

    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.warn("", e);
            }
        }
    }

    public void sendToClient(String sid, String message) {
        Session session = sessionMap.get(sid);
        if (session == null) {
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public void sendToClient(String sid, WsData message) {
        sendToClient(sid, message.toString());
    }
}