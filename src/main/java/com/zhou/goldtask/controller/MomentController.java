package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.zhou.goldtask.entity.MomentCreateRequest;
import com.zhou.goldtask.service.MomentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/page/moment")
@CrossOrigin
@SaCheckLogin
@Slf4j
public class MomentController {
    @Resource
    private MomentService momentService;

    @GetMapping("/list")
    public SaResult list(@RequestParam(value = "page", defaultValue = "1") Integer page) {
        return momentService.list(StpUtil.getLoginIdAsString(), page);
    }

    @PostMapping("/upload")
    public SaResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "thumb", required = false) MultipartFile thumb) {
        return momentService.upload(StpUtil.getLoginIdAsString(), file, thumb);
    }

    @PostMapping
    public SaResult create(@RequestBody MomentCreateRequest body) {
        return momentService.create(StpUtil.getLoginIdAsString(), body);
    }

    @DeleteMapping("/{id}")
    public SaResult delete(@PathVariable String id) {
        return momentService.delete(StpUtil.getLoginIdAsString(), id);
    }

    @GetMapping("/file/{fileId}")
    public void file(@PathVariable String fileId, HttpServletRequest request, HttpServletResponse response) {
        try {
            momentService.writeFile(StpUtil.getLoginIdAsString(), fileId, request, response);
        } catch (IOException ex) {
            log.warn("朋友圈文件输出失败", ex);
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
