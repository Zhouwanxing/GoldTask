package com.zhou.goldtask.controller;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.ITaskService;
import com.zhou.goldtask.service.Mp4Service;
import com.zhou.goldtask.service.UrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collections;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/mp4")
@CrossOrigin
public class Mp4Controller {
    @Resource
    private Mp4Service mp4Service;
    @Resource
    private UrlService urlService;
    @Resource
    private ITaskService taskService;

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
    public JSONObject pageShowList(@RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "devId", defaultValue = "") String devId) {
        if (mp4Service.isMyDev(devId)) {
            return new JSONObject().putOpt("list", mp4Service.pageShowList(page)).putOpt("count", mp4Service.count());
        } else {
            taskService.remindTask("设备码", devId);
            return new JSONObject().putOpt("list", Collections.emptyList()).putOpt("count", 0);
        }
    }

    @GetMapping("/toNotLike")
    public JSONObject toNotLike(@RequestParam(value = "id") String id) {
        mp4Service.toNotLike(id);
        return new JSONObject().putOpt("success", true);
    }
}
