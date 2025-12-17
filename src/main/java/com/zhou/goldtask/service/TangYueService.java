package com.zhou.goldtask.service;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.ErPageFormEntity;
import com.zhou.goldtask.entity.ErSFHistoryEntity;
import com.zhou.goldtask.entity.TangYueEntity;
import com.zhou.goldtask.entity.TangYueOneHome;
import com.zhou.goldtask.repository.TangYueDao;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TangYueService {
    @Autowired
    private OcrService ocrService;
    @Autowired
    private TangYueDao tangYueDao;
    @Autowired
    private MongoTemplate secondMongoTemplate;

    public List<JSONObject> getAJK(ErPageFormEntity data) {
        Query query = new Query();
        query.addCriteria(Criteria.where("area").gte(data.getAreaMin()).lte(data.getAreaMax()));
        query.addCriteria(Criteria.where("price").gte(data.getPriceMin()).lte(data.getPriceMax()));
        if (data.getShowFloor() == 1) {
            query.addCriteria(Criteria.where("floor").is("低"));
        } else if (data.getShowFloor() == 2) {
            query.addCriteria(Criteria.where("floor").is("中"));
        } else if (data.getShowFloor() == 3) {
            query.addCriteria(Criteria.where("floor").is("高"));
        }
        if (data.getShowLike() == 1) {
            query.addCriteria(Criteria.where("like").isNull());
        }
        query.fields().exclude("title", "createTime");
        query.with(Sort.by(data.getSortValue() == 1 ? Sort.Direction.ASC : Sort.Direction.DESC, data.getSortKey()));
        log.info("{}", query);
        List<JSONObject> list = secondMongoTemplate.find(query, JSONObject.class, "my_ersf");
        Query hisQuery = new Query();
        hisQuery.with(Sort.by(Sort.Direction.ASC, "time"));
        hisQuery.fields().exclude("_id");
        List<ErSFHistoryEntity> ts = secondMongoTemplate.find(hisQuery, ErSFHistoryEntity.class);
        for (JSONObject one : list) {
            one.putOpt("histories", ts.stream().filter(t -> one.getStr("_id").equals(t.getHomeId()))
                    .collect(Collectors.toList()));
        }
        return list;
    }

    public void updateHome(JSONObject updateData) {
        String id = updateData.getStr("id");
        String type = updateData.getStr("type");
        if ("delete".equals(type)) {
            secondMongoTemplate.remove(new Query(Criteria.where("_id").is(id)), "my_ersf");
            secondMongoTemplate.remove(new Query(Criteria.where("homeId").is(id)), "my_ersf_history");
            return;
        }
        secondMongoTemplate.updateFirst(new Query(Criteria.where("_id").is(id)),
                new Update().set("like", false), "my_ersf");
    }

    public List<List<Object>> getList(TangYueEntity data) {
        List<TangYueEntity> list = tangYueDao.findList(data);
        List<List<Object>> table = new ArrayList<>();
        List<Object> oneRow = new ArrayList<>();
        oneRow.add("楼层");
        Optional<TangYueEntity> first;
        for (int i = 0; i < 8; i++) {
            int finalI = i;
            first = list.stream().filter(t -> ((finalI + 1) + "").equals(t.getRoom())).findFirst();
            if (first.isPresent()) {
                oneRow.add(first.get().getAreaReal());
            } else {
                break;
            }
        }
        if (oneRow.size() == 1) {
            return table;
        }
        table.add(oneRow);
        for (int i = 0; i < 50; i++) {
            List<Object> floor = getOneFloor(i + 1, list, oneRow.size() - 1);
            if (floor == null) {
                if (i < 10) {
                    continue;
                }
                break;
            }
            table.add(floor);
        }
        return table;
    }

    private List<Object> getOneFloor(int i, List<TangYueEntity> list, int roomCount) {
        Stream<TangYueEntity> stream = list.stream().filter(t -> t.getFloor().equals((i < 10 ? "0" : "") + i));
        if (!stream.findAny().isPresent()) {
            return null;
        }
        List<Object> oneRow = new ArrayList<>();
        oneRow.add(i);
        for (int j = 0; j < roomCount; j++) {
            int finalJ = j;
            Optional<TangYueEntity> first = list.stream().filter(t -> t.getFloor().equals((i < 10 ? "0" : "") + i)).filter(t -> ((finalJ + 1) + "").equals(t.getRoom())).findFirst();
            if (first.isPresent()) {
                oneRow.add(first.get().getPrice());
            } else {
                oneRow.add(0);
            }
        }
        return oneRow;
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