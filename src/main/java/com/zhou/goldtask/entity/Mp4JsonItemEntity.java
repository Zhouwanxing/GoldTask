package com.zhou.goldtask.entity;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class Mp4JsonItemEntity {
    private int id;
    private int classid;
    private int newstime;
    private String title;
    private String titlepic;
    private String yulan;
    private String m3u8;


    public String newSTimeToDate() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(newstime), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}