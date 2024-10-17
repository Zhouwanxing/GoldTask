package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.UrlService;
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
    private UrlService urlService;

    @GetMapping("/testA")
    public JSONObject testA() {
        JSONObject a = new JSONObject();
        a.putOpt("a", "ccc");
        return a;
    }

    @GetMapping("/checkNewUrl")
    public JSONObject checkNewUrl() {
        urlService.checkNewUrl();
        return new JSONObject().putOpt("success", true);
    }
}