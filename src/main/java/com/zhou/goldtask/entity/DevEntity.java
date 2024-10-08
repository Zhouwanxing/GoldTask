package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@TableName("my_dev")
@Document("my_dev")
public class DevEntity {
    @Id
    private String _id;
    private String name;
}
