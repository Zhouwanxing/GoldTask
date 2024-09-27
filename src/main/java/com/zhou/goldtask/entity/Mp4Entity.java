package com.zhou.goldtask.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "my_mp4")
public class Mp4Entity {
    @Id
    private String _id;

    private String name;
}