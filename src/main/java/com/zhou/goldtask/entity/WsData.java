package com.zhou.goldtask.entity;

import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WsData {
    private String id;
    private String type;
    private String message;
    private String from;
    private String to;
    /** 对战玩法：checkers（默认）或 pool。空着时按跳棋处理，旧客户端不用改。 */
    private String game;

    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}