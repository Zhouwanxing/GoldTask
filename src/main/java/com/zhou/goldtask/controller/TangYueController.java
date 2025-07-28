package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.TangYueEntity;
import com.zhou.goldtask.service.TangYueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/tangyue")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("tangyue")
public class TangYueController {
    @Resource
    private TangYueService tangYueService;

    @PostMapping("/getList")
    public SaResult getLikeList(@RequestBody TangYueEntity data) {
        SaResult ok = SaResult.ok();
        ok.setData(tangYueService.getList(data));
        return ok;
    }

    @PostMapping("/getAJK")
    public SaResult getAJK(@RequestBody JSONObject data) {
        SaResult ok = SaResult.ok();
        ok.setData(tangYueService.getAJK(data.getInt("area"),data.getInt("price")));
        return ok;
    }

    @PostMapping("/updateErSF")
    public SaResult updateHome(@RequestBody JSONObject updateData) {
        tangYueService.updateHome(updateData);
        return SaResult.ok();
    }
}