package com.zhou.goldtask.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.EnvConfig;
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
    private EnvConfig envConfig;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private GoldService goldService;

    @Override
    public void run(String... args) throws Exception {
        String urlString = "https://api.day.app/" + envConfig.getBarkId() + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(Utils.dateTimeFormat)) + "服务启动";
        try {
            log.info("{},{}", HttpUtil.get(urlString), urlString);
        } catch (Exception e) {
            log.warn("{}", urlString, e);
        }
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
    }
}