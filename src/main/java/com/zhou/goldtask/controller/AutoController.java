package com.zhou.goldtask.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import com.zhou.goldtask.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/auto")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("auto")
public class AutoController {
    @Resource
    private AutoService autoService;

    @GetMapping("/getInfo")
    public SaResult getInfo() {
        return SaResult.data(autoService.getInfo());
    }

    @GetMapping("/inWork")
    public SaResult inWork() {
        autoService.inWork();
        return SaResult.ok();
    }

}