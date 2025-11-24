package com.zhou.goldtask.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * https://bark.day.app/#/?id=bark
 */
@Service
@Slf4j
@ConditionalOnProperty(prefix = "my", name = "task", havingValue = "bark")
public class BarkTaskService implements ITaskService {
    @Resource
    private EnvConfig envConfig;

    @Override
    public void remindTask(String title, String body, String group, boolean isAutoSave) {
        String urlString = "https://api.day.app/" + envConfig.getBarkId();
        String data = JSONUtil.toJsonStr(new JSONObject().putOpt("body", body).putOpt("group", group).putOpt("title", title).putOpt("isArchive", isAutoSave ? "1" : ""));
        try {
            log.info("{},{},{}", HttpUtil.post(urlString, data), urlString, data);
        } catch (Exception e) {
            log.warn("{},{}", urlString, data, e);
        }
    }
}