package com.zhou.goldtask.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    @RequestMapping("/heartbeat")
    public String heartbeat() {
        return "success";
    }
}