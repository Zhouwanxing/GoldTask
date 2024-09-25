package com.zhou.goldtask.service;

import cn.hutool.core.util.StrUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MyService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private EnvConfig envConfig;

    public void saveStartUrl(String url) {
        if (StrUtil.isBlankIfStr(url)) {
            return;
        }
        /*String[] split = url.split("//");
        String host = split[1].split("/")[0];
        if (StrUtil.isBlankIfStr(host)) {
            return;
        }
        String newUrl = split[0] + "//" + host;*/
        redisTemplate.opsForList().rightPush(Utils.UrlRedisKey, url);
        AllGoldData.getInstance().addUrl(url);
    }

    public List<String> getUrls(String key, boolean isCheck) {
        if (!isCheck) {
            return AllGoldData.getInstance().getUrls();
        }
        if (key != null && key.length() > 5 && envConfig.getCheckKey().equals(key)) {
            return AllGoldData.getInstance().getUrls();
        }
        return new ArrayList<>();
    }

    public void deleteUrl(String url) {
        redisTemplate.opsForList().remove(Utils.UrlRedisKey, 0, url);
        AllGoldData.getInstance().removeUrl(url);
    }
}
