package com.zhou.goldtask.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.repository.GoldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/gold")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("gold")
public class GoldController {
    @Resource
    private GoldRepository goldRepository;

    @GetMapping("/allGold")
    public SaResult allGold() {
        JSONObject a = new JSONObject();
        List<GoldEntity> list = goldRepository.findAll();
        a.putOpt("list", list);
        a.putOpt("size", list.size());
        return SaResult.data(a);
    }
}