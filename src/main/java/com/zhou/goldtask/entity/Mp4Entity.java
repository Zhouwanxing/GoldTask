package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@TableName("my_mp4")
@Document("my_mp4")
@Slf4j
public class Mp4Entity {
    @Id
    private String _id;
    private String name;
    private String path;
    private String url;
    private String date;
    private String img;
    private String insertTime;
    private String href;
    private String flag;

    public Mp4Entity urlToId() {
        if (url != null) {
            try {
                String[] split = url.split("/");
                this._id = split[split.length - 3] + "/" + split[split.length - 2];
            } catch (Exception e) {
                log.warn("{}", url, e);
            }
        }
        return this;
    }

    public Mp4Entity dateToDate() {
        if (date != null) {
            date = LocalDate.now().getYear() + "-" + date;
        }
        return this;
    }

    public String getIdStartDate() {
        if (_id == null || !_id.contains("/")) {
            return "";
        }
        return _id.split("/")[0];
    }
}