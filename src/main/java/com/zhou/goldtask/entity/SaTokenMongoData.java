package com.zhou.goldtask.entity;

import cn.dev33.satoken.session.SaSession;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document("my_saToken")
@Slf4j
@Setter
@Getter
public class SaTokenMongoData {

    @Id
    private String id;

    // token
    @Indexed(unique = true)
    private String key;

    // sa-token 的 session
    private SaSession session;

    // sa-token 的 token string
    private String string;

    //使用 @SuppressWarnings("removal") 的目的是，防止IDEA报错，因为 expireAfterSeconds是不在支持的属性。
    // 给 expireAt 添加 `@Indexed(expireAfterSeconds = 0)` 注解，当过期时MongoDB会自动帮我删除过期的数据
    @Indexed(expireAfterSeconds = 0)
    private Date expireAt; // 你也可以使用 Date 类型，对应的在`SaTokenMongoDao`中，需要将LocalDateTime替换成Date
}