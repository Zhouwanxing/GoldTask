package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@TableName("my_mp4")
@Document("my_mp4")
public class Mp4Entity {
    @Id
    private String _id;
    private String name;
    private String path;
    private String url;
    private String date;
    private String img;

    public Mp4Entity urlToId() {
        if (url != null) {
            String[] split = url.split("/");
            this._id = split[split.length - 3] + "/" + split[split.length - 2];
        }
        return this;
    }

    public Mp4Entity dateToDate() {
        if (date != null) {
            date = LocalDate.now().getYear() + "-" + date;
        }
        return this;
    }
}