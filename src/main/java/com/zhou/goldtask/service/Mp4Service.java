package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.Mp4Entity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class Mp4Service {
    @Resource
    private MongoService mongoService;

    public Mp4Entity findOne() {
        return null;
//        return mongoTemplate.findOne(new Query(), Mp4Entity.class);
    }

    public boolean save(Mp4Entity mp4Entity) {
        return mongoService.saveOne(mp4Entity, Mp4Entity.class);
    }

    public void genNew() {
        List<String> urls = AllGoldData.getInstance().getUrls();
        for (String url : urls) {
            genNewOne(url);
        }
    }

    private void genNewOne(String url) {
        log.info("{} start.", url);
        try {
            Document doc = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.190 Safari/537.36")
                    .timeout(5000)
                    .get();
            log.info("{}", doc.body());
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public void ajaxUrl(String url) {
        log.info("{} start.", url);
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            log.info("{}", doc.body());
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}
