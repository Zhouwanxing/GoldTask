package com.zhou.goldtask.controller;

import com.zhou.goldtask.entity.Mp4Entity;
import com.zhou.goldtask.service.Mp4Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/save")
    public String save() {
        String s = Math.random() + "";
        mp4Service.save(Mp4Entity.builder()._id(s).name("test" + s).build());
        return "test";
    }
}
