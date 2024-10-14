package com.zhou.goldtask.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/user")
@CrossOrigin
public class UserController {
    @Resource
    private UserService userService;

    @RequestMapping("/heartbeat")
    public String heartbeat() {
        return "success";
    }

    @RequestMapping("/login")
    public SaResult doLogin(String username, String password) {
        if (userService.isExistUser(username, password)) {
            StpUtil.login(username);
            return SaResult.data(getUserInfo());
        }
        return SaResult.error();
    }

    private JSONObject getUserInfo() {
        JSONObject jsonObject = new JSONObject();
        SaTokenInfo info = StpUtil.getTokenInfo();
        jsonObject.set("tokenName", info.getTokenName());
        jsonObject.set("tokenValue", info.getTokenValue());
        jsonObject.set("roles", StpUtil.getPermissionList());
        return jsonObject;
    }

    @RequestMapping("/isLogin")
    public SaResult isLogin() {
        return StpUtil.isLogin() ? SaResult.data(getUserInfo()) : SaResult.error();
    }

    @RequestMapping("/logout")
    public SaResult logOut() {
        StpUtil.logout();
        return SaResult.ok();
    }
}