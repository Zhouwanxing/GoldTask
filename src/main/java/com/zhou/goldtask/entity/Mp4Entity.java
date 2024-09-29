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
    private String path;
    private String url;

    public Mp4Entity urlToId() {
        if (url != null) {
            String[] split = url.split("/");
            this._id = split[split.length - 2];
        }
        return this;
    }
}