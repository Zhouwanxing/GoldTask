package com.zhou.goldtask.entity;

import lombok.Data;

import java.util.List;

@Data
public class Mp4BaseEntity {
    private String tbname;
    private List<Mp4BaseItemEntity> items;
}
