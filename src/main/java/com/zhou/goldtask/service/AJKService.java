package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.CJEntity;
import com.zhou.goldtask.entity.ErSFEntity;
import com.zhou.goldtask.entity.ErSFHistoryEntity;
import com.zhou.goldtask.utils.RuntimeData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public void cut() {
        List<ErSFEntity> list = secondMongoTemplate.find(new Query(), ErSFEntity.class);
        String floor = null;
        for (ErSFEntity one : list) {
            if (one.getInfo().contains("楼层")) {
                floor = String.valueOf(one.getInfo().charAt(one.getInfo().indexOf("楼层") - 1));
            } else {
                floor = String.valueOf(one.getInfo().charAt(one.getInfo().indexOf("层") - 1));
            }
            one.setFloor(floor);
            secondMongoTemplate.save(one);
        }
    }

    private JSONObject ajkInfo() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("ajk"));
        return mongoTemplate.findOne(query, JSONObject.class, "system_config");
    }

    public void check() {
        List<ErSFEntity> list = secondMongoTemplate.find(new Query(), ErSFEntity.class);
        for (ErSFEntity erSFEntity : list) {
            if (!erSFEntity.getLinkUrl().contains(erSFEntity.get_id())) {
                System.out.println(erSFEntity);
            }
        }
    }



    public void startAjk() {
        JSONObject ajkInfo = ajkInfo();
        if (ajkInfo == null) {
            return;
        }
        try {
            String cookie = RuntimeData.getInstance().getAjkCookie(ajkInfo.getStr("baseUrl"));
            if (StringUtils.isBlank(cookie)) {
                return;
            }
            List<JSONObject> urls = ajkInfo.getJSONArray("value").toList(JSONObject.class);
            /*for (JSONObject urlObj : urls) {
                String url = urlObj.getStr("url");
                log.info("{}", url);
                String body = HttpRequest.get(url).cookie(cookie).timeout(10000).execute().body();
                log.info("{}\n{}", url, body);
                handleOneContent(body);
            }*/
            String url = urls.get(DateUtil.thisHour(true) % 2).getStr("url");
            log.info("{}", url);
            String body = HttpRequest.get(url).cookie(cookie).timeout(10000).execute().body();
            handleOneContent(body);
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    public void handleFtx() {
        JSONObject ajkInfo = ajkInfo();
        if (ajkInfo == null) {
            return;
        }
        String ftxValue = ajkInfo.getStr("ftxValue");
        String body = HttpUtil.get(ftxValue + "/house/kw%E5%94%90%E6%A8%BE/?refer=sy_seach", 5000);
        handleOneFtx(body, ftxValue);
        Elements pageBox = Jsoup.parse(body).getElementsByClass("page_al").get(0).getElementsByTag("span");
        Elements a = null;
        for (Element box : pageBox) {
            a = box.getElementsByTag("a");
            if (a.size() == 1) {
                body = HttpUtil.get(ftxValue + a.get(0).attr("href"), 5000);
                handleOneFtx(body, ftxValue);
            }
        }
    }

    private void handleOneFtx(String body, String ftxValue) {
        try {
            Elements dl = Jsoup.parse(body).getElementsByClass("shop_list shop_list_4").get(0).getElementsByTag("dl");
            ErSFEntity one = null;
            for (Element element : dl) {
                one = new ErSFEntity();
                one.syncFtx(element, ftxValue);
                saveToDB(one);
            }
        } catch (Exception ignored) {
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
//                handleOneContent(content);
                handleLJContent(content);
            }
        }
    }

    public void handleLJContent(String content) {
        try {
            Document parse = Jsoup.parse(content);
            Element ul = parse.getElementsByClass("sellListContent").get(0);
            Elements li = ul.getElementsByTag("li");
            ErSFEntity lj = null;
            for (Element element : li) {
                lj = ErSFEntity.builder().build();
                lj.handLJ(element);
                System.out.println(lj);
                saveToDB(lj);
            }
        } catch (Exception e) {

        }
    }

    public void handleLJCJ(String url, String body) {
        if (!url.contains("%E4%B8%87%E7%A7%91%E6%B1%89%E5%8F%A3%E4%BC%A0%E5%A5%87%E5%94%90%E6%A8%BE")) {
            log.warn("{}", url);
            return;
        }
        double price = 10000.0;
        if (url.contains("ep")) {
            try {
                price = Double.parseDouble(url.substring(url.indexOf("ep") + 2, url.indexOf("ep") + 6));
            } catch (Exception e) {
                try {
                    price = Double.parseDouble(url.substring(url.indexOf("ep") + 2, url.indexOf("ep") + 5));
                } catch (Exception e1) {
                    price = Double.parseDouble(url.substring(url.indexOf("ep") + 2, url.indexOf("ep") + 4));
                }
            }
        }
//        log.info("price:{}", price);
//        System.out.println(body);
        try {
            Document bo = Jsoup.parse(body);
            Elements listContent = bo.getElementsByClass("listContent");
            Element element = listContent.get(0);
            Elements li = element.getElementsByTag("li");
            for (Element one : li) {
                cjHandle(one,price);
            }
            System.out.println("=="+li.size());
        } catch (Exception ignored) {
        }
    }

    private void cjHandle(Element one, double price) {
        CJEntity entity = CJEntity.builder().build();
        boolean success = entity.parseCJ(one);
        if (!success) {
            return;
        }
        CJEntity old = secondMongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(entity.get_id())), CJEntity.class);
        if (old != null) {
            if (old.getPrice() < price) {
                return;
            }
        }
        entity.handlePrice(price);
        secondMongoTemplate.save(entity);
        log.info("{}", entity);
    }

    public void syncLj(ErSFEntity entity) {
        try {
            entity.syncLj();
            saveToDB(entity);
        } catch (Exception ignored) {
        }
    }

    private void handleOneContent(String content) {
        Document parse = Jsoup.parse(content);
        Elements elements = parse.getElementsByAttributeValue("tongji_tag", "fcpc_ersflist_gzcount");
        if (elements.size() == 0) {
            log.info("{}", content);
        }
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
        saveToDB(ersfEntity);
    }

    private void saveToDB(ErSFEntity ersfEntity) {
        if (ersfEntity.get_id() != null) {
            /*String repeatId = getRepeatHomeId(ersfEntity);
            if (repeatId != null) {
                ersfEntity.set_id(repeatId);
            }*/
            ErSFEntity old = secondMongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(ersfEntity.get_id())), ErSFEntity.class);
            if (old != null) {
                if (old.getPrice() != ersfEntity.getPrice()) {
                    ErSFHistoryEntity his = ErSFHistoryEntity.builder()._id(UUID.fastUUID().toString()).price(old.getPrice()).homeId(ersfEntity.get_id()).time(DateUtil.now()).build();
                    secondMongoTemplate.save(his);
                }
                ersfEntity.setLike(old.getLike());
                ersfEntity.setCreateTime(old.getCreateTime());
            } else {
                ersfEntity.setCreateTime(DateUtil.now());
                taskService.remindTask(ersfEntity.getInfo(), ersfEntity.getPriceStr(), ersfEntity.getFrom(), ersfEntity.getLinkUrl(), true);
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
        if ("lj".equals(ersfEntity.getFrom())) {
            return null;
        }
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
            String str = obj.getJSONObject("exposure").getStr("vpid");
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