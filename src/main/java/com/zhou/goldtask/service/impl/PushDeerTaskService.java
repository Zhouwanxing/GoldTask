package com.zhou.goldtask.service.impl;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "my", name = "task", havingValue = "pushdeer")
public class PushDeerTaskService implements ITaskService {
    @Resource
    private EnvConfig envConfig;

    @Override
    public void remindTask(String title, String body) {
        String urlString = "https://api2.pushdeer.com/message/push?pushkey=" + envConfig.getPushDeerId() + "&text=" + title + body;
        try {
            log.info("{},{}", HttpUtil.get(urlString), urlString);
        } catch (Exception e) {
            log.warn("{}", urlString, e);
        }
    }
}