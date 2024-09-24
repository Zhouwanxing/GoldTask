package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.service.MyService;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RestController
@RequestMapping("/page")
@CrossOrigin
public class MyController {
    @Resource
    private MyService myService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/testA")
    public JSONObject testA() {
        JSONObject a = new JSONObject();
        a.putOpt("a", "ccc");
        return a;
    }

    @GetMapping("/allGold")
    public JSONObject allGold() {
        JSONObject a = new JSONObject();
        List<GoldEntity> list = AllGoldData.getInstance().getList();
        a.putOpt("list", list);
        a.putOpt("size", list.size());
        return a;
    }

    @GetMapping("/allRedisGold")
    public JSONObject allRedisGold() {
        Long size = redisTemplate.opsForList().size(Utils.goldRedisKey);
        List<GoldEntity> list = new ArrayList<>();
        JSONObject a = new JSONObject();
        if (size == null || size == 0) {
            a.putOpt("list", list);
            a.putOpt("size", 0);
            return a;
        }
        for (int i = 0; i < size; i++) {
            list.add(JSONUtil.toBean(redisTemplate.opsForList().index(Utils.goldRedisKey, i), GoldEntity.class));
        }
        a.putOpt("list", list);
        a.putOpt("size", size);
        return a;
    }

    @PostMapping("/initData")
    public JSONObject initData(@RequestBody JSONObject data) {
        log.info("{}", data);
        myService.saveStartUrl(data.getStr("url",""));
        return new JSONObject().putOpt("success", true);
    }
}