package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.Mp4Entity;
import com.zhou.goldtask.entity.Mp4LikeDto;
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
        mp4Service.genNew(null);
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
    public SaResult pageShowList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "showLike", required = false) boolean isShowLike
    ) {
        SaResult result = SaResult.ok();
        result.set("list", mp4Service.pageShowList(page, isShowLike));
        result.set("count", mp4Service.count(isShowLike));
        return result;
    }

    @GetMapping("/toNotLike")
    public SaResult toNotLike(@RequestParam(value = "id") String id) {
        mp4Service.updateLike(id, false);
        return SaResult.ok();
    }


    @GetMapping("/updateLike")
    public SaResult updateLike(@RequestParam(value = "id") String id, @RequestParam(value = "like") boolean isLike) {
        mp4Service.updateLike(id, isLike);
        return SaResult.ok();
    }

    @PostMapping("/saveUrl")
    public SaResult initData(@RequestBody JSONObject data) {
        log.info("{}", data);
        urlService.addUrl(data.getStr("url", ""));
        return getUrls();
    }

    @PostMapping("/getUrls")
    public SaResult getUrls() {
        return SaResult.data(urlService.getUrls());
    }

    @PostMapping("/deleteUrl")
    public SaResult deleteUrl(@RequestBody JSONObject data) {
        urlService.deleteUrl(data.getStr("url"));
        return getUrls();
    }

    @GetMapping("/getAllPath")
    public SaResult getAll() {
        return SaResult.data(mp4Service.getAllPath());
    }

    @PostMapping("/getLikeList")
    public SaResult getLikeList(@RequestBody Mp4LikeDto data) {
        SaResult ok = SaResult.ok();
        ok.setData(mp4Service.searchLike(data));
        ok.set("count", mp4Service.searchLikeCount(data));
        return ok;
    }
}