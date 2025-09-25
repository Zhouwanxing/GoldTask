package com.zhou.goldtask.entity;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Mp4JsonItemEntity {
    private int id;
    private int classid;
    private int newstime;
    private String title;
    private String titlepic;
    private String yulan;
    private String m3u8;
    private String mp4;
    private String keyboard;


    public String newSTimeToDate() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(newstime), ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public List<String> handleKeyboard() {
        List<String> list = new ArrayList<>();
        if (keyboard != null && !"".equals(keyboard)) {
            list.addAll(Arrays.asList(keyboard.split(",")));
        }
        return list;
    }
}