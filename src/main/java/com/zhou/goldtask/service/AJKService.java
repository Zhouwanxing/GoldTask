package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.ErSFEntity;
import com.zhou.goldtask.entity.ErSFHistoryEntity;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class AJKService {
    @Resource
    private MongoTemplate secondMongoTemplate;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ITaskService taskService;

    private JSONObject ajkInfo() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("ajk"));
        return mongoTemplate.findOne(query, JSONObject.class, "system_config");
    }

    public void startAjk() {
        JSONObject ajkInfo = ajkInfo();
        if (ajkInfo == null) {
            return;
        }
        try {
            String cookie = ajkInfo.getStr("cookie");
            List<String> urls = ajkInfo.getJSONArray("value").toList(String.class);
            for (String url : urls) {
                String body = HttpRequest.get(url).cookie(cookie).execute().body();
                log.info("{}\n{}", url, body);
                handleOneContent(body);
            }
        } catch (Exception e) {
            log.warn("", e);
        }
    }

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
        ErSFEntity ersfEntity = ErSFEntity.builder()._id(getHomeId(element)).title(getTitle(element)).info(getInfo(element)).linkUrl(getLinkUrl(element)).lastTime(DateUtil.now()).priceStr(getPrice(element)).build();
        ersfEntity.makeOther();
        System.out.println(ersfEntity);
        if (ersfEntity.get_id() != null) {
            String repeatId = getRepeatHomeId(ersfEntity);
            if (repeatId != null) {
                ersfEntity.set_id(repeatId);
            }
            ErSFEntity old = secondMongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(ersfEntity.get_id())), ErSFEntity.class);
            if (old != null && old.getPrice() != ersfEntity.getPrice()) {
                ErSFHistoryEntity his = ErSFHistoryEntity.builder()._id(UUID.fastUUID().toString()).price(old.getPrice()).homeId(ersfEntity.get_id()).time(DateUtil.now()).build();
                secondMongoTemplate.save(his);
            }
            if (old == null) {
                taskService.remindTask(DateUtil.now(), ersfEntity.toString(), true);
            }
            secondMongoTemplate.save(ersfEntity);
        }
    }

    private String getLinkUrl(Element element) {
        try {
            return element.getElementsByTag("a").get(0).attr("href");
        } catch (Exception e) {
            return null;
        }
    }

    private String getRepeatHomeId(ErSFEntity ersfEntity) {
        Query query = new Query();
        query.addCriteria(Criteria.where("title").is(ersfEntity.getTitle()));
        query.addCriteria(Criteria.where("area").is(ersfEntity.getArea()));
        query.addCriteria(Criteria.where("info").is(ersfEntity.getInfo()));
        query.addCriteria(Criteria.where("_id").ne(ersfEntity.get_id()));
        ErSFEntity one = secondMongoTemplate.findOne(query, ErSFEntity.class);
        return one == null ? null : one.get_id();
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