package com.zhou.goldtask.service;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.TangYueEntity;
import com.zhou.goldtask.entity.TangYueOneHome;
import com.zhou.goldtask.repository.TangYueDao;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TangYueService {
    @Autowired
    private OcrService ocrService;
    @Autowired
    private TangYueDao tangYueDao;

    public List<TangYueEntity> getList(TangYueEntity data) {
        return tangYueDao.findList(data);
    }


    public List<String> getOneAs(String url) {
        List<String> list = new ArrayList<>();
        try {
            String page = HttpUtil.get(url);
            Document doc = Jsoup.parse(page);

            Elements elements = doc.getElementsByTag("a");
            String href = null;
            for (Element element : elements) {
                href = element.attr("href");
                if (!"#".equals(href) && !href.contains("000000000000")) {
                    list.add(href);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public TangYueOneHome getHomeInfo(String url) {
        String[] split = url.split("/");
        String host = split[0] + "//" + split[2];
        System.out.println(url);
        String page = HttpUtil.get(url);
        Document doc = Jsoup.parse(page);
        String attr = doc.getElementsByTag("a").get(0).attr("href");
        page = HttpUtil.get(host + attr);
//        System.out.println(page);
        Elements tr = Jsoup.parse(page).getElementsByTag("tr");
        TangYueOneHome home = TangYueOneHome.builder().build();
        String text = null;
        for (Element element : tr) {
            text = element.text();
            if (text.contains("房屋座落")) {
                home.setAllId(element.getElementsByTag("td").get(1).text());
            } else if (text.contains("实测面积")) {
                home.setAreaReal(Double.parseDouble(element.getElementsByTag("td").get(1).text()));
            } else if (text.contains("预测面积")) {
                home.setAreaPredict(Double.parseDouble(element.getElementsByTag("td").get(1).text()));
            } else if (text.contains("单价")) {
                home.setPrice((int) Double.parseDouble(ocrService.getUrlText(host + "/" + element.getElementsByTag("img").get(0).attr("src"))));
            }
        }
        if (home.get_id() == null) {
            return null;
        }
        return home;
    }
}