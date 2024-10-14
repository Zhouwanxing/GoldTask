package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.Mp4Service;
import com.zhou.goldtask.service.UrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/mp4")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("mp4")
public class Mp4Controller {
    @Resource
    private Mp4Service mp4Service;
    @Resource
    private UrlService urlService;

    @RequestMapping("/test")
    public String test() {
        mp4Service.saveOne();
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

    @GetMapping("/getNewStart")
    public JSONObject getNewStart() {
        urlService.checkNewUrl();
        return new JSONObject().putOpt("success", true);
    }

    @GetMapping("/pageShowList")
    public JSONObject pageShowList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                   @RequestParam(value = "showLike", required = false) boolean isShowLike
    ) {
        return new JSONObject().putOpt("list", mp4Service.pageShowList(page, isShowLike)).putOpt("count", mp4Service.count(isShowLike));
    }

    @GetMapping("/toNotLike")
    public JSONObject toNotLike(@RequestParam(value = "id") String id) {
        mp4Service.updateLike(id, false);
        return new JSONObject().putOpt("success", true);
    }


    @GetMapping("/updateLike")
    public JSONObject updateLike(@RequestParam(value = "id") String id, @RequestParam(value = "like") boolean isLike) {
        mp4Service.updateLike(id, isLike);
        return new JSONObject().putOpt("success", true);
    }
}
