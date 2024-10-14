package com.zhou.goldtask.entity;

import com.zhou.goldtask.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@TableName("my_user")
@Document("my_user")
public class UserEntity {
    @Id
    private String _id;
    private String password;
    private List<String> roles;
}