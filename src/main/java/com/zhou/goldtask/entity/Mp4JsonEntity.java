package com.zhou.goldtask.entity;

import lombok.Data;

import java.util.List;

@Data
public class Mp4JsonEntity {
    public String category_name;
    public int category_tempid;
    public String category_type;
    public int current_page;
    public int from;
    public int last_page;
    public int per_page;
    public int to;
    public int total;
    public List<Mp4JsonItemEntity> data;
}
