package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@TableName("my_new_mp4")
@Document("my_new_mp4")
@Slf4j
public class Mp4NewEntity {
    @Id
    private String _id;
    private int classid;
    private String date;
    private String name;
    private String img;
    private String yulan;
    private String m3u8;
    private String url;
    private String path;
    private List<String> tags;
}