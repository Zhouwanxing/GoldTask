package com.zhou.goldtask.entity;

import lombok.Data;

@Data
public class SwingConfig {
    private String _id;
    private String broker;
    private String topic;
    private String messageSecret;
}