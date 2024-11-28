package com.zhou.goldtask.service;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.repository.GoldRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;

@Service
@Slf4j
public class GoldService {
    @Resource
    private GoldRepository goldRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ITaskService taskService;

    public GoldEntity getTodayGold() {
        GoldEntity item = goldRepository.findItemById(LocalDate.now().toString());
        return item == null ? GoldEntity.builder()._id("").zdf(0).zss(0).build() : item;
    }

    public void genToDayGold() {
        //周生生
        String oneP = "", twoP = "", body = "";
        int zss = 0, zdf = 0;
        try {
            body = HttpUtil.get("https://ws.chowsangsang.com/goldpriceapi/goldprice-poss/openapi/v1/list?region=CHN");
            oneP = JSONUtil.parseObj(body).getJSONArray("data").stream().filter(one -> "G_JW_SELL".equals(((JSONObject) one).getStr("type"))).map(one -> ((JSONObject) one).getStr("price")).findFirst().get();
            zss = (int) Double.parseDouble(oneP);
        } catch (Exception e) {
            log.warn("", e);
        }
        //周大福
        try {
            body = HttpUtil.get("https://api.ctfmall.com/wxmini/api/common/todayGoldPrice?action=gettodayprice");
            twoP = JSONUtil.parseObj(body).getJSONObject("data").getStr("todayPriceHK");
            zdf = (int) Double.parseDouble(twoP);
        } catch (Exception e) {
            log.warn("", e);
        }
        goldRepository.save(GoldEntity.builder()._id(LocalDate.now().toString()).zss(zss).zdf(zdf).build());
        taskService.remindTask(LocalDate.now().toString(), "周生生:" + zss + ";周大福:" + zdf + ";占用:" + getMongoUse(), true);
    }

    public int getCcb() {
        try {
            String cookie = HttpRequest.get("https://gold2.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=NGJS01")
                    .header("Cookie", "tranCCBIBS1=DFAfiPr7u9HaynOy9toVyYvbyaVqK10%2CtVkhlKSIu8U0ubLdxDHtSjUE5GUqlAYOt4UVlIXSurEQtMXitnURpZVoutER12gvPJjYIk; ")
                    .execute().body();
            return JSONUtil.parseObj(cookie).getInt("Cst_Buy_Prc");
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return 0;
    }

    public void goldTask() {
        String content = "周生生:" + getTodayGold().getZss() + ";周大福:" + getTodayGold().getZdf() + ";占用:" + getMongoUse();
        taskService.remindTask(LocalDate.now().toString(), content, true);
    }

    private String getMongoUse() {
        try {
            Document document = mongoTemplate.executeCommand("{ dbStats: 1 }");
            long totalSize = document.getLong("dataSize") + document.getLong("indexSize");
            double mb = (double) totalSize / 1024 / 1024;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return mb > 1 ? decimalFormat.format(mb) + "MB" : decimalFormat.format((double) totalSize / 1024) + "KB";
        } catch (Exception e) {
            return "";
        }
    }
}