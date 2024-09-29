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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class Mp4Service {
    @Resource
    private MongoService mongoService;

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
            Document doc = Jsoup.connect(url + "/indexaKo.js")
                    .timeout(5000)
                    .get();
            Elements menu = doc.getElementsByClass("menu");
            if (menu.size() == 0) {
                return;
            }
            Elements channels = null, elements = menu.get(0).getElementsByTag("a");
            List<String> urls = new ArrayList<>();
            String menuHref = null, mp4Href = null, textLink = null, download = null, date = null, img = null;
            String[] textLinks = null;
            for (Element element : elements) {
                menuHref = element.attr("href");
                if (menuHref.contains("javascript") || menuHref.contains("/pic/")) {
                    continue;
                }
                channels = Jsoup.connect(url + menuHref).timeout(5000).get().getElementsByClass("preview-item");
                if (channels.size() == 0) {
                    continue;
                }
                for (Element channel : channels) {
                    mp4Href = channel.getElementsByTag("a").attr("href");
                    date = channel.getElementsByTag("i").text();
                    img = channel.getElementsByTag("img").attr("data-original");
                    if (urls.contains(mp4Href)) {
                        continue;
                    }
                    urls.add(mp4Href);
                    log.info(mp4Href);
                    doc = Jsoup.connect(url + mp4Href)
                            .timeout(5000)
                            .get();
                    textLinks = doc.getElementsByClass("textlink").get(0).html().split("&nbsp;&nbsp;");
                    textLink = textLinks[textLinks.length - 1];
                    download = doc.getElementsByClass("download").get(0).getElementsByTag("a").get(0).attr("href");
                    if (textLink == null || "".equals(textLink) || "".equals(download)) {
                        log.info("{}\n{}", mp4Href, doc.body());
                    } else {
                        mongoService.saveOne(Mp4Entity.builder()
                                .name(textLink)
                                .path(menuHref)
                                .url(download)
                                .date(date)
                                .img(img)
                                .build().urlToId().dateToDate(), Mp4Entity.class);
                        Thread.sleep(500);
                    }
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
