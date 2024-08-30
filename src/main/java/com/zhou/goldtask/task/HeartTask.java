package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class HeartTask {
    @Scheduled(cron = "0/5 * * * * ?")
    public void remindTaskRun() {
        try {
            log.info(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info(HttpUtil.get("https://goldtask.onrender.com/"));
        } catch (Exception ignored) {

        }
    }
}