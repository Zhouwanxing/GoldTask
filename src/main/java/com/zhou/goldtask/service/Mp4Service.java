package com.zhou.goldtask.service;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.Mp4Entity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
            Document doc = Jsoup.connect(url.endsWith("/") ? url + "indexaKo.js" : url + "/indexaKo.js")
                    .timeout(5000)
                    .get();
            Elements menu = doc.getElementsByClass("menu");
            if (menu.size() == 0) {
                return;
            }
            Elements elements = menu.get(0).getElementsByTag("a");
            String href = null;
            for (Element element : elements) {
                href = element.attr("href");
                if (href.contains("javascript") || href.contains("/pic/")) {
                    continue;
                }
                log.info("{}", href);
                doc = Jsoup.connect(url + href)
                        .timeout(5000)
                        .get();
                Elements channels = doc.getElementsByClass("channel-list");
                if (channels.size() == 0) {
                    continue;
                }
                Elements tag = channels.get(0).getElementsByTag("a");
                for (Element element1 : tag) {
                    String href1 = element1.attr("href");
                    log.info(href1);
                    doc = Jsoup.connect(url + href1)
                            .timeout(5000)
                            .get();
                    String text = doc.getElementsByClass("textlink").text();
                    log.info("{}", text);
                    String download = doc.getElementsByClass("download").text();
                    log.info("{}", download);
                }
            }
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
            String s = HttpUtil.get(url, 5000);
            log.info("{}", s);
        }
    }
}
