package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.Mp4Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/mp4")
@CrossOrigin
public class Mp4Controller {
    @Resource
    private Mp4Service mp4Service;

    @RequestMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/genNew")
    public JSONObject genNew() {
        mp4Service.genNew();
        return new JSONObject().putOpt("success", true);
    }


    @PostMapping("/ajaxUrl")
    public JSONObject ajaxUrl(@RequestBody JSONObject jsonObject) {
        mp4Service.ajaxUrl(jsonObject.getStr("url"));
        return new JSONObject().putOpt("success", true);
    }
}
