package com.zhou.goldtask.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.ErSFEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Service
public class AJKService {
    @Resource
    private MongoTemplate secondMongoTemplate;

    public void readWebToDB(String fileName) {
        String s = FileUtil.readString(fileName, StandardCharsets.UTF_8);
        JSONArray list = JSONUtil.parseObj(s).getJSONObject("log").getJSONArray("entries");
        JSONObject oneRow;
        String content;
        for (int i = 0; i < list.size(); i++) {
            oneRow = list.getJSONObject(i);
            content = getContent(oneRow);
            if (content != null && !"".equals(content)) {
                System.out.println(oneRow.getJSONObject("request").getStr("url"));
                handleOneContent(content);
            }
        }
    }

    private void handleOneContent(String content) {
        Document parse = Jsoup.parse(content);
        Elements elements = parse.getElementsByAttributeValue("tongji_tag", "fcpc_ersflist_gzcount");
        for (Element element : elements) {
            handleOneRow(element);
        }
    }

    private void handleOneRow(Element element) {
        if (!isTy(element)) {
            return;
        }
        String homeId = getHomeId(element);
        ErSFEntity ersfEntity = ErSFEntity.builder()._id(homeId).title(getTitle(element)).info(getInfo(element)).priceStr(getPrice(element)).build();
        ersfEntity.makeOther();
        System.out.println(ersfEntity);
        secondMongoTemplate.save(ersfEntity);
        /*if (homeId != null) {
            System.out.println(element.html());
        } else {
            System.out.println(element.html());
        }*/
    }

    private String getInfo(Element element) {
        try {
            return element.getElementsByClass("property-content-info").get(0).text();
        } catch (Exception e) {
            return null;
        }
    }

    private String getPrice(Element element) {
        try {
            return element.getElementsByClass("property-price").get(0).text();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTy(Element element) {
        try {
            return element.getElementsByClass("property-content-info-comm-name").get(0).text().contains("唐樾");
        } catch (Exception e) {
            return false;
        }
    }

    private String getTitle(Element element) {
        try {
            return element.getElementsByTag("h3").get(0).attr("title");
        } catch (Exception e) {
            return null;
        }
    }

    private String getHomeId(Element element) {
        try {
            Element a = element.getElementsByAttributeValue("data-action", "esf_list").get(0);
            String attr = a.attr("data-ep");
            JSONObject obj = JSONUtil.parseObj(attr);
            String str = obj.getJSONObject("exposure").getStr("owner_house_id");
            if (str == null) {
                str = JSONUtil.parseObj(a.attr("data-lego")).getStr("entity_id");
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getContent(JSONObject oneRow) {
        JSONObject object = oneRow.getJSONObject("response").getJSONObject("content");
        String mimeType = object.getStr("mimeType");
        if ("text/html".equals(mimeType)) {
            return object.getStr("text");
        }
        return "";
    }
}