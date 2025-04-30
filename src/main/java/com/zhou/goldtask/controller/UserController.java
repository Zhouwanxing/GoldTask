package com.zhou.goldtask.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.*;
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
    @Resource
    private ChromeService chromeService;

    @PostMapping("/mz")
    public SaResult mz(@RequestBody JSONObject data) {
//        log.info("{}", data);
        JSONObject resData = data.getJSONObject("data");
//        log.info("{}", resData.get("url"));
//        log.info("{}", resData.get("cookie"));
        String body = HttpRequest.get(resData.getStr("url")).cookie(resData.getStr("cookie")).execute().body();
        System.out.println(body);
        /*for (String s : body.split("\n")) {
            if (s != null && s.contains("window.__PRELOADED_STATE__")) {
                System.out.println(s);
            }
        }*/
        return SaResult.ok();
    }

    @RequestMapping("/heartbeat")
    public String heartbeat() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() % 10 == 0) {
            userService.mongoTest();
        }
        return "s";
    }

    @RequestMapping("/login")
    public SaResult doLogin(String username, String password, @RequestParam(required = false, defaultValue = "", value = "dev") String dev) {
        if (userService.isExistUser(username, password)) {
            StpUtil.login(username, dev);
            return SaResult.data(getUserInfo(dev));
        }
        return SaResult.error();
    }

    private JSONObject getUserInfo(String dev) {
        JSONObject jsonObject = new JSONObject();
        SaTokenInfo info = StpUtil.getTokenInfo();
        jsonObject.set("tokenName", info.getTokenName());
        jsonObject.set("tokenValue", info.getTokenValue());
        jsonObject.set("roles", StpUtil.getPermissionList());
        if ("swing".equals(dev)) {
            jsonObject.set("swingConfig", userService.getSwingConfig());
        }
        return jsonObject;
    }

    @RequestMapping("/isLogin")
    public SaResult isLogin() {
        return StpUtil.isLogin() ? SaResult.data(getUserInfo("")) : SaResult.error();
    }

    @RequestMapping("/logout")
    public SaResult logOut() {
        StpUtil.logout();
        return SaResult.ok();
    }

    @GetMapping("/getNewStart")
    public SaResult getNewStart() {
        urlService.checkNewUrl(false);
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

    @GetMapping("/runChrome")
    public SaResult runChrome(String url) {
        chromeService.runChromeUrl(url);
        return SaResult.ok();
    }


    @GetMapping("/sendToMq")
    public SaResult sendToMq(String message, @RequestParam(required = false, defaultValue = "0") int qos) {
        userService.sendToMqtt(message, qos);
        return SaResult.ok();
    }
}