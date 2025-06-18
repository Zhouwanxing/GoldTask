package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import com.zhou.goldtask.entity.UrlEntity;
import com.zhou.goldtask.repository.Mp4Dao;
import com.zhou.goldtask.repository.UrlRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
public class UrlService {
    @Resource
    private UrlRepository urlRepository;
    @Resource
    private Mp4Service mp4Service;
    @Resource
    private Mp4Dao mp4Dao;


    public void checkNewUrl(boolean isDown) {
        List<String> urls = mp4Dao.getUrls();
        Set<String> addUrls = new HashSet<>();
        String newUrl;
        for (String url : urls) {
            newUrl = getUrlLocation(url);
            if (newUrl != null && newUrl.startsWith("http") && !urls.contains(newUrl)) {
                addUrls.add(newUrl);
                break;
            }
            newUrl = getEnUrl(url);
            if (newUrl != null && newUrl.startsWith("http") && !urls.contains(newUrl)) {
                addUrls.add(newUrl);
                break;
            }
        }
        for (String addUrl : addUrls) {
            addUrl(addUrl);
            urls.add(0, addUrl);
        }
        if (isDown) {
            mp4Service.genNew(urls);
        }
    }

    public void deleteUrl(String url) {
        urlRepository.deleteById(url);
    }

    public void addUrl(String url) {
        if (StrUtil.isBlankIfStr(url)) {
            return;
        }
        try {
            urlRepository.insert(UrlEntity.builder()._id(url).date(DateUtil.now()).build());
        } catch (Exception ignored) {
        }
    }

    private String getUrlLocation(String url) {
        log.info(url);
        try {
            if (url.contains("154.88.28.8")) {
                return getEnUrl(url);
            } else {
                Map<String, List<String>> headers = HttpRequest.get(url).execute().headers();
                headers.keySet().forEach(key -> log.info(key + ":" + headers.get(key)));
                List<String> location = headers.get("Location");
                return location != null && location.size() > 0 ? location.get(0) : "";
            }
        } catch (Exception e) {
            log.warn("", e);
        }
        return "";
    }


    private String getEnUrl(String url) {
        try {
            String body = HttpRequest.get(url).execute().body();
            log.info(body);
            if (body != null && body.contains("window.atob")) {
                String en = body.substring(body.indexOf("window.atob") + 13, body.indexOf("\"", body.indexOf("window.atob(") + 15));
                return new String(Base64.getDecoder().decode(en));
            }
        } catch (Exception ignored) {

        }
        return null;
    }
}