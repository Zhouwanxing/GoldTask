package com.zhou.goldtask.service;

import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class StartedRunner implements CommandLineRunner {
    @Resource
    private ITaskService taskService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private GoldService goldService;

    @Override
    public void run(String... args) throws Exception {
        taskService.remindTask(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Utils.dateTimeFormat)), "服务启动");
        redisToMem();
    }

    private void redisToMem() {
        Long size = redisTemplate.opsForList().size(Utils.goldRedisKey);
        if (size == null || size == 0) {
            goldService.genToDayGold();
            return;
        }
        String index = null;
        for (int i = 0; i < size; i++) {
            index = redisTemplate.opsForList().index(Utils.goldRedisKey, i);
            AllGoldData.getInstance().add(JSONUtil.toBean(index, GoldEntity.class));
        }
        log.info("allGoldData size:{}", AllGoldData.getInstance().getList().size());

        size = redisTemplate.opsForList().size(Utils.UrlRedisKey);
        if (size == null || size == 0) {
            return;
        }
        for (int i = 0; i < size; i++) {
            AllGoldData.getInstance().addUrl(redisTemplate.opsForList().index(Utils.UrlRedisKey, i));
        }
        log.info("urlData size:{}", AllGoldData.getInstance().getUrls().size());

    }
}