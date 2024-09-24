package com.zhou.goldtask.service;

import cn.hutool.core.util.StrUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class MyService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    public void saveStartUrl(String url) {
        String[] split = url.split("//");
        String host = split[1].split("/")[0];
        if (StrUtil.isBlankIfStr(host)) {
            return;
        }
        String newUrl = split[0] + "//" + host;
        redisTemplate.opsForList().rightPush(Utils.UrlRedisKey, newUrl);
        AllGoldData.getInstance().addUrl(newUrl);
    }
}
