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

    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}