package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.MyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MyService {
    private MyConfig myConfig;

    public void initConfig(MyConfig config) {
        this.myConfig = config;
        log.info("初始化配置成功");
    }

    public MyConfig getMyConfig() {
        return myConfig == null ? new MyConfig() : myConfig;
    }
}
