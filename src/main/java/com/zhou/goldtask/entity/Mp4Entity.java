package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("my_mp4")
public class Mp4Entity {
    private String _id;

    private String name;
}