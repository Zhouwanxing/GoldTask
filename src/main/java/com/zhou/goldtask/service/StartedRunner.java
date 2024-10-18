package com.zhou.goldtask.service;

import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class StartedRunner implements CommandLineRunner {
    @Resource
    private ITaskService taskService;

    @Override
    public void run(String... args) throws Exception {
        taskService.remindTask(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Utils.dateTimeFormat)), "服务启动", false);
    }
}