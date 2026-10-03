package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.zhou.goldtask.entity.MarbleDeviceDto;
import com.zhou.goldtask.service.MarbleBankService;
import com.zhou.goldtask.service.MarbleConfigService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * 弹珠库存。列表和加珠要登录；设备自己查询和上报不需要登录。
 */
@RestController
@RequestMapping("/page/marble")
@CrossOrigin
@Slf4j
public class MarbleController {
    @Resource
    private MarbleBankService marbleBankService;
    @Resource
    private MarbleConfigService marbleConfigService;

    @SaCheckLogin
    @GetMapping("/list")
    public SaResult list() {
        return SaResult.data(marbleBankService.list());
    }

    @SaCheckLogin
    @PostMapping("/add")
    public SaResult add(@RequestBody AddBody body) {
        try {
            MarbleDeviceDto dto = marbleBankService.add(body == null ? null : body.getSid(), body == null ? null : body.getDelta());
            log.info("弹珠加减珠: sid={}, delta={}, count={}, pending={}, known={}", dto.getSid(), body == null ? null : body.getDelta(), dto.getCount(), dto.getPending(), dto.isKnown());
            return SaResult.data(dto);
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        }
    }

    @GetMapping("/get")
    public SaResult get(@RequestParam String sid) {
        try {
            return SaResult.data(marbleBankService.get(sid));
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        }
    }

    @PostMapping("/sync")
    public SaResult sync(@RequestBody MarbleBankService.SyncCommand body) {
        try {
            return SaResult.data(marbleBankService.sync(body));
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        }
    }

    /** 获胜概率设备端也要读，不要求登录。 */
    @GetMapping("/config")
    public SaResult config() {
        return SaResult.data(Collections.singletonMap("winChance", marbleConfigService.getWinChance()));
    }

    @SaCheckLogin
    @PostMapping("/config")
    public SaResult saveConfig(@RequestBody ConfigBody body) {
        try {
            int winChance = marbleConfigService.saveWinChance(body == null ? null : body.getWinChance());
            log.info("弹珠获胜概率调整: {}%", winChance);
            return SaResult.data(Collections.singletonMap("winChance", winChance));
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        }
    }

    @Data
    public static class AddBody {
        private String sid;
        private Integer delta;
    }

    @Data
    public static class ConfigBody {
        private Integer winChance;
    }
}
