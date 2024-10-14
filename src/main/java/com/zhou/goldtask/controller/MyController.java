package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.MyService;
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
    private MyService myService;
    @Resource
    private UrlService urlService;

    @GetMapping("/testA")
    public JSONObject testA() {
        JSONObject a = new JSONObject();
        a.putOpt("a", "ccc");
        return a;
    }

    @PostMapping("/initData")
    public JSONObject initData(@RequestBody JSONObject data) {
        log.info("{}", data);
        urlService.addUrl(data.getStr("url", ""));
        return new JSONObject().putOpt("success", true);
    }

    @PostMapping("/getUrls")
    public JSONObject getUrls(@RequestBody JSONObject data) {
        return new JSONObject().putOpt("urls", myService.getUrls(data.getStr("key"), true));
    }

    @PostMapping("/deleteUrl")
    public JSONObject deleteUrl(@RequestBody JSONObject data) {
        myService.deleteUrl(data.getStr("url"));
        return new JSONObject().putOpt("urls", myService.getUrls(null, false));
    }

    @GetMapping("/checkNewUrl")
    public JSONObject checkNewUrl() {
        urlService.checkNewUrl();
        return new JSONObject().putOpt("success", true);
    }
}