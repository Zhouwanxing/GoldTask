package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.MyConfig;
import com.zhou.goldtask.service.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
public class MyController {
    @Resource
    private MyService myService;

    @PostMapping("/initConfig")
    public String initConfig(@RequestBody JSONObject config) {
        myService.initConfig(config.toBean(MyConfig.class));
        return "success";
    }
}
