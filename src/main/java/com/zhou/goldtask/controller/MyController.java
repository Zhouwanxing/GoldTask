package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RestController
@RequestMapping("/page")
@CrossOrigin
public class MyController {
    @GetMapping("/testA")
    public JSONObject testA() {
        JSONObject a = new JSONObject();
        a.putOpt("a", "ccc");
        return a;
    }
}