package com.zhou.goldtask.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class GoldEntity {
    private String date;
    private int zdf;
    private int zss;
}