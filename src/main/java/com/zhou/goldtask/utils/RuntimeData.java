package com.zhou.goldtask.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.zhou.goldtask.entity.SwingConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuntimeData {
    private static RuntimeData instance = null;

    private SwingConfig swingConfig;

    private String ajkCookie;
    private final List<String> onlineDay = new ArrayList<>();


    public void online() {
        onlineDay.add(DateUtil.today() + (DateUtil.thisHour(true) < 10 ? "0" : "1"));
    }

    public boolean isOnline() {
        return onlineDay.contains(DateUtil.today() + (DateUtil.thisHour(true) < 10 ? "0" : "1"));
    }

    private RuntimeData() {
        instance = this;
    }

    public static RuntimeData getInstance() {
        if (instance == null) {
            new RuntimeData();
        }
        return instance;
    }

    public SwingConfig getSwingConfig() {
        return swingConfig;
    }

    public void setSwingConfig(SwingConfig swingConfig) {
        this.swingConfig = swingConfig;
    }

    public String getAjkCookie(String baseUrl) {
        String current = null;
        try {
            Map<String, List<String>> headers = HttpRequest.get(baseUrl).timeout(5000).execute().headers();
            List<String> cookies = headers.get("Set-Cookie");
            current = String.join(";", cookies);
        } catch (Exception ignored) {
        }
        if (StringUtils.isNotBlank(current)) {
            this.ajkCookie = current;
        }
        return this.ajkCookie;
    }
}