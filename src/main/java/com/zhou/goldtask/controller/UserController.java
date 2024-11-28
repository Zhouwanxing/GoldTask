package com.zhou.goldtask.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.FileService;
import com.zhou.goldtask.service.Mp4Service;
import com.zhou.goldtask.service.UrlService;
import com.zhou.goldtask.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/user")
@CrossOrigin
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private UrlService urlService;
    @Resource
    private FileService fileService;
    @Resource
    private Mp4Service mp4Service;

    @RequestMapping("/heartbeat")
    public String heartbeat() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() % 10 == 0) {
            userService.mongoTest();
        }
        return "s";
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

    @GetMapping("/getNewStart")
    public SaResult getNewStart() {
        urlService.checkNewUrl();
        return SaResult.ok();
    }

    @GetMapping("/getFileContent")
    public SaResult getAllUrl(String fileName) {
        return SaResult.data(fileService.getFileContentList(fileName));
    }


    @GetMapping("/genOtherPage")
    public SaResult genOtherPage(String url, String menuHref, @RequestParam(defaultValue = "1") int page) {
        if (StrUtil.isAllNotBlank(url, menuHref)) {
            mp4Service.oneType(url, menuHref, page);
        }
        return SaResult.ok();
    }
}