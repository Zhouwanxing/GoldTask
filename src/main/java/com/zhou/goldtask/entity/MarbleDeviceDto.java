package com.zhou.goldtask.entity;

import lombok.Data;

/** 管理页和设备查询共用的弹珠库存快照。 */
@Data
public class MarbleDeviceDto {
    private String sid;
    private String name;
    private int count;
    private int pending;
    private boolean known;
    private boolean online;
}
