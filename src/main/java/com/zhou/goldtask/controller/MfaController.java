package com.zhou.goldtask.controller;

import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.service.MfaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * MFA 验证码接口（与 mfa.html 约定一致，无需鉴权）
 * GET  /page/mfa/list
 * POST /page/mfa/saveOne
 * POST /page/mfa/deleteOne
 */
@Controller
@Slf4j
@RestController
@RequestMapping("/page/mfa")
@CrossOrigin
public class MfaController {
    @Resource
    private MfaService mfaService;

    @GetMapping("/list")
    public SaResult list() {
        return SaResult.data(mfaService.list());
    }

    @PostMapping("/saveOne")
    public SaResult saveOne(@RequestBody JSONObject body) {
        try {
            mfaService.saveOne(body);
            return SaResult.ok();
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        } catch (Exception e) {
            log.warn("保存 MFA 失败", e);
            return SaResult.error("保存失败");
        }
    }

    @PostMapping("/deleteOne")
    public SaResult deleteOne(@RequestBody JSONObject body) {
        try {
            String id = body == null ? null : body.getStr("id");
            if (id == null && body != null) {
                id = body.getStr("_id");
            }
            mfaService.deleteOne(id);
            return SaResult.ok();
        } catch (IllegalArgumentException e) {
            return SaResult.error(e.getMessage());
        } catch (Exception e) {
            log.warn("删除 MFA 失败", e);
            return SaResult.error("删除失败");
        }
    }
}
