package com.zhou.goldtask.service;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.repository.GoldRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;

@Service
@Slf4j
public class GoldService {
    @Resource
    private GoldRepository goldRepository;

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
    }
}
