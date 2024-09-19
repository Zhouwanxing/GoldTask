package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.ChromeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/chrome")
@CrossOrigin
public class ChromeController {
    @Resource
    private ChromeService chromeService;

    @GetMapping("/getState/{devId}")
    public JSONObject getState(@PathVariable String devId) {
        return chromeService.getState(devId);
    }

    @GetMapping("/code/{devId}/{code}")
    public String code(@PathVariable String devId, @PathVariable String code) {
        chromeService.toChromeCode(devId, code);
        return "success";
    }
}