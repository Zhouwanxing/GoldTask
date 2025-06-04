package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.Mp4Entity;
import com.zhou.goldtask.entity.Mp4LikeDto;
import com.zhou.goldtask.entity.PathCountEntity;
import com.zhou.goldtask.entity.UrlEntity;
import com.zhou.goldtask.repository.Mp4Dao;
import com.zhou.goldtask.repository.Mp4Repository;
import com.zhou.goldtask.repository.UrlRepository;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private FileService fileService;
    @Resource
    private UrlRepository urlRepository;

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

    public List<Mp4Entity> pageShowList(Integer page, boolean isShowLike, String path) {
        return mp4Dao.findByPage(page, isShowLike, path);
//        return handleList(mp4Dao.findByPage(page, isShowLike));
    }

    public List<Mp4Entity> searchLike(Mp4LikeDto dto) {
        return mp4Dao.findByDto(dto);
    }

    public long searchLikeCount(Mp4LikeDto dto) {
        return mp4Dao.searchLikeCount(dto);
    }

    public long count(boolean isShowLike, String path) {
        return mp4Dao.count(isShowLike, path);
    }

    public void genNew(List<String> urls) {
        if (urls == null || urls.size() == 0) {
            urls = mp4Dao.getUrls();
        }
        for (String url : urls) {
            if (handleOther(url)) {
                break;
            }
        }
    }

    public boolean handleOther(String url) {
        List<String> paths = mp4Dao.distinctPath();
        boolean isIn = false;
        for (String path : paths) {
            if (oneType(url, path)) {
                isIn = true;
            }
        }
        if(isIn && DateUtil.thisDayOfMonth() == 28){
            startCheckNo(url);
        }
        return isIn;
    }

    public void startCheckNo(String url) {
        int m = DateUtil.thisMonth() + 1;
        String mon = m > 10 ? "" + m : "0" + m;
        String start = "/html/" + DateUtil.thisYear() + mon + "/";
        Set<String> keys = stringRedisTemplate.keys(Utils.Mp4RedisKey + start + "*");
        if (keys == null) {
            return;
        }
        List<Integer> num = new ArrayList<>();
        String[] split, split1;
        String last;
        for (String key : keys) {
            try {
                split = key.split("/");
                last = split[split.length - 1];
                split1 = last.split("\\.");
                num.add(Integer.parseInt(split1[0]));
            } catch (Exception ignored) {

            }
        }
        List<Integer> collect = num.stream().sorted().collect(Collectors.toList());
        log.info("{}", collect.size());
        for (int i = collect.get(0); i < collect.get(collect.size() - 1); i++) {
            if (!num.contains(i)) {
                handleOneLast(url + start + i + ".html", "", "", "");
            }
        }
    }

    public boolean oneType(String url, String menuHref, int page) {
        try {
            String newUrl = url + menuHref + (page > 1 ? "index_" + page + ".html" : "");
            Elements channels = Jsoup.connect(newUrl).timeout(5000).get().getElementsByClass("preview-item");
            log.info("{}\n{}", newUrl, channels.size());
            if (channels.size() == 0) {
                return false;
            }
            String mp4Href;
            for (Element channel : channels) {
                mp4Href = channel.getElementsByTag("a").attr("href");
                if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(Utils.Mp4RedisKey + mp4Href))) {
                    continue;
                }
                stringRedisTemplate.opsForValue().set(Utils.Mp4RedisKey + mp4Href, menuHref, 200, TimeUnit.DAYS);
                handleOneLast(url + mp4Href,
                        channel.getElementsByTag("i").text(),
                        channel.getElementsByTag("img").attr("data-original"), menuHref);
            }
            return true;
        } catch (Exception e) {
            log.warn("", e);
            return false;
        }
    }

    private boolean oneType(String url, String menuHref) {
        if (menuHref == null || "".equals(menuHref)) {
            return false;
        }
        boolean b = oneType(url, menuHref, 1);
        oneType(url, menuHref, 2);
        return b;
    }

    private void handleOneLast(String url, String date, String img, String menuHref) {
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            String[] textLinks = doc.getElementsByClass("textlink").get(0).html().split("&nbsp;&nbsp;");
            String textLink = textLinks[textLinks.length - 1];
            String download = doc.getElementsByClass("download").get(0).getElementsByTag("a").get(0).attr("href");
            String href = url;
            try {
                href = url.split(".com")[1];
            } catch (Exception ignored) {

            }
            if (textLink == null || "".equals(textLink) || "".equals(download)) {
                log.info("{}\n{}", url, doc.body());
            } else {
                Mp4Entity entity = Mp4Entity.builder()
                        .name(textLink)
                        .path(menuHref)
                        .url(download)
                        .date(date)
                        .img(img)
                        .href(href)
                        .insertTime(DateUtil.now())
                        .build().urlToId().dateToDate();
                if (idNotExists(entity)) {
                    try {
                        mp4Repository.insert(entity);
                    } catch (Exception ignored) {

                    }
                }
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    private boolean idNotExists(Mp4Entity entity) {
        String fileName = entity.getIdStartDate();
        if (fileName == null || "".equals(fileName)) {
            return true;
        }
        boolean exists = fileService.getFileContentList(fileName).contains(entity.get_id());
        if (exists) {
            log.info("文件检测存在该id:{}", entity.get_id());
        }
        return !exists;
    }

    public void ajaxUrl(String url) {
        log.info("{} start.", url);
        try {
            Document doc = Jsoup.connect(url).timeout(5000).get();
            log.info("{}", doc.body());
        } catch (Exception e) {
            try {
                String s = HttpUtil.get(url, 5000);
                log.info("{}", s);
            } catch (Exception ignored) {

            }
        }
    }

    public void updateLike(String id, String flag) {
        mp4Dao.updateLike(id, flag);
    }

    public List<String> getAllPath() {
        return mp4Dao.getAllPath();
    }

    public List<PathCountEntity> getAllCountAndPath(){
        return mp4Dao.getAllCountAndPath();
    }

    public String getInXxUrl(String id) {
        Mp4Entity mp4 = mp4Repository.findById(id).orElse(null);
        if (mp4 != null) {
            String href = mp4.getHref();
            if (StringUtils.isNotBlank(href)) {
                UrlEntity oneSort = urlRepository.findSort(Pageable.ofSize(1)).getContent().get(0);
                if (oneSort != null) {
                    return oneSort.get_id() + href;
                }
            }
        }
        return null;
    }
}