package com.zhou.goldtask.entity;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class EnvConfig {
    @Value("${TASK_NAME:z}")
    private String taskName;

    @Value("${PUSH_DEER_ID:z}")
    private String pushDeerId;

    @Value("${BARK_ID:z}")
    private String barkId;


    @Value("${CHECK_KEY:z}")
    private String checkKey;

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;
    @Value("${HOST_NAME:}")
    private String hostName;
    @Value("${MY_KEY:}")
    private String myKey;
}
