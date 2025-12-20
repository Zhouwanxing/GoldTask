package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.Mp4LikeDto;
import com.zhou.goldtask.repository.Mp4Dao;
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
    @Resource
    private Mp4Dao mp4Dao;

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

    @GetMapping("/pageShowList")
    public SaResult pageShowList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "showLike", required = false) boolean isShowLike,
                                 @RequestParam(value = "path", required = false, defaultValue = "") String path
    ) {
        SaResult result = SaResult.ok();
        result.set("list", mp4Service.pageShowList(page, isShowLike, path));
        result.set("count", mp4Service.count(isShowLike, path));
        return result;
    }

    @GetMapping("/toNotLike")
    public SaResult toNotLike(@RequestParam(value = "id") String id) {
        mp4Service.updateLike(id, "delete");
        return SaResult.ok();
    }


    @GetMapping("/updateLike")
    public SaResult updateLike(@RequestParam(value = "id") String id, @RequestParam(value = "flag",defaultValue = "good",required = false) String flag) {
        mp4Service.updateLike(id, flag);
        return SaResult.ok();
    }

    @GetMapping("/updateDuration")
    public SaResult updateDuration(@RequestParam(value = "id") String id,
                                   @RequestParam(value = "duration", defaultValue = "0", required = false) Double duration) {
        if (duration == null || duration < 0) {
            return SaResult.ok();
        }
        mp4Service.updateDuration(id, duration);
        return SaResult.ok();
    }

    @GetMapping("/getInXxUrl")
    public SaResult getInXxUrl(@RequestParam(value = "id") String id) {
        SaResult ok = SaResult.ok();
        ok.setData(mp4Service.getInXxUrl(id));
        return ok;
    }

    @PostMapping("/saveUrl")
    public SaResult initData(@RequestBody JSONObject data) {
        log.info("{}", data);
        urlService.addUrl(data.getStr("url", ""));
        return getUrls();
    }

    @PostMapping("/getUrls")
    public SaResult getUrls() {
        return SaResult.data(mp4Dao.getUrls());
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

    @GetMapping("/getAllCountAndPath")
    public SaResult getAllCountAndPath() {
        return SaResult.data(mp4Service.getAllCountAndPath());
    }

    @PostMapping("/getLikeList")
    public SaResult getLikeList(@RequestBody Mp4LikeDto data) {
        log.info("{}", data);
        SaResult ok = SaResult.ok();
        ok.setData(mp4Service.searchLike(data));
        ok.set("count", mp4Service.searchLikeCount(data));
        return ok;
    }
}