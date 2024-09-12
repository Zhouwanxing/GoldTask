package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.MyConfig;
import com.zhou.goldtask.service.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page")
@CrossOrigin
public class MyController {
    @Resource
    private MyService myService;

    @PostMapping("/initConfig")
    public String initConfig(@RequestBody JSONObject config) {
        myService.initConfig(config.toBean(MyConfig.class));
        return "success";
    }

    @GetMapping("/testA")
    public JSONObject testA() {
        JSONObject a = new JSONObject();
        a.putOpt("a", "ccc");
        return a;
    }
}