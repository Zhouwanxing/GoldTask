package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.zhou.goldtask.entity.MarbleDeviceDto;
import com.zhou.goldtask.service.MarbleBankService;
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
            log.info("弹珠加珠: sid={}, count={}, pending={}, known={}", dto.getSid(), dto.getCount(), dto.getPending(), dto.isKnown());
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

    @Data
    public static class AddBody {
        private String sid;
        private Integer delta;
    }
}
