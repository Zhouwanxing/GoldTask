package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@TableName("my_gold")
@Document("my_gold")
public class GoldEntity {
    @Id
    private String _id;
    private int zdf;
    private int zss;
    private int ccb;
}