package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.Mp4Entity;
import com.zhou.goldtask.entity.Mp4LikeDto;
import com.zhou.goldtask.repository.Mp4Dao;
import com.zhou.goldtask.repository.Mp4Repository;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class Mp4Service {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private Mp4Repository mp4Repository;
    @Resource
    private Mp4Dao mp4Dao;
    @Resource
    private UrlService urlService;

    public void saveOne() {
        Mp4Entity entity = Mp4Entity.builder()
                .name("textLink")
                .path("menuHref")
                .url("https://zzzz/zz/zz/zz/zz")
                .date("date")
                .img("img")
                .build().urlToId().dateToDate();
        mp4Repository.save(entity);
    }

    public List<Mp4Entity> pageShowList(Integer page, boolean isShowLike) {
        return handleList(mp4Dao.findByPage(page, isShowLike));
    }

    private List<Mp4Entity> handleList(List<Mp4Entity> list) {
        for (Mp4Entity mp4 : list) {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(Utils.Mp4ImgRedisKey + mp4.get_id()))) {
                mp4.setImg(stringRedisTemplate.opsForValue().get(Utils.Mp4ImgRedisKey + mp4.get_id()));
            } else {
                mp4.setImg(setMp4Url(mp4));
            }
        }
        return list;
    }

    public List<Mp4Entity> searchLike(Mp4LikeDto dto) {
        return handleList(mp4Dao.findByDto(dto));
    }

    public long searchLikeCount(Mp4LikeDto dto) {
        return mp4Dao.searchLikeCount(dto);
    }

    public long count(boolean isShowLike) {
        return mp4Dao.count(isShowLike);
    }

    private String setMp4Url(Mp4Entity mp4) {
        String url = mp4.getImg();
        try {
            if (url == null || "".equals(url)) {
                return "";
            }
            log.info("start:{}", url);
            String data = HttpUtil.get(url, 1000);
            if (data != null && !"".equals(data)) {
                stringRedisTemplate.opsForValue().set(Utils.Mp4ImgRedisKey + mp4.get_id(), data, 1, TimeUnit.DAYS);
                return data;
            }
        } catch (Exception e) {
            log.warn("", e);
        }
        return url;
    }

    public void genNew() {
        List<String> urls = urlService.getUrls();
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
                handleOther(url);
                return;
            }
            Elements elements = menu.get(0).getElementsByTag("a");
            String menuHref = null;
            for (Element element : elements) {
                menuHref = element.attr("href");
                if (menuHref.contains("javascript") || menuHref.contains("/pic/")
                        || menuHref.contains("/dongman/") || menuHref.contains("/leisi/")
                        || menuHref.contains("/sm/") || menuHref.contains("/nxx/")
                        || menuHref.contains("/giga/") || menuHref.contains("/youma/")) {
                    continue;
                }
                oneType(url, menuHref);
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    private void handleOther(String url) {
        List<String> paths = mp4Dao.distinctPath();
        for (String path : paths) {
            oneType(url, path);
        }
    }

    private void oneType(String url, String menuHref) {
        try {
            Elements channels = Jsoup.connect(url + menuHref).timeout(5000).get().getElementsByClass("preview-item");
            if (channels.size() == 0) {
                return;
            }
            String mp4Href = null;
            for (Element channel : channels) {
                mp4Href = channel.getElementsByTag("a").attr("href");
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(Utils.Mp4RedisKey + mp4Href))) {
                    continue;
                }
                stringRedisTemplate.opsForValue().set(Utils.Mp4RedisKey + mp4Href, "1", 7, TimeUnit.DAYS);
                handleOneLast(url + mp4Href,
                        channel.getElementsByTag("i").text(),
                        channel.getElementsByTag("img").attr("data-original"), menuHref);
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    private void handleOneLast(String url, String date, String img, String menuHref) {
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            String[] textLinks = doc.getElementsByClass("textlink").get(0).html().split("&nbsp;&nbsp;");
            String textLink = textLinks[textLinks.length - 1];
            String download = doc.getElementsByClass("download").get(0).getElementsByTag("a").get(0).attr("href");
            if (textLink == null || "".equals(textLink) || "".equals(download)) {
                log.info("{}\n{}", url, doc.body());
            } else {
                mp4Repository.insert(Mp4Entity.builder()
                        .name(textLink)
                        .path(menuHref)
                        .url(download)
                        .date(date)
                        .img(img)
                        .insertTime(DateUtil.now())
                        .build().urlToId().dateToDate());
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

    public void updateLike(String id, boolean isLike) {
        mp4Dao.updateLike(id, isLike);
        if (!isLike) {
            stringRedisTemplate.delete(Utils.Mp4RedisKey + id);
        }
    }

    public List<String> getAllPath() {
        return mp4Dao.getAllPath();
    }
}