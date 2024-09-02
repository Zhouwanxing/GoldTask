package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.service.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class GoldTask {
    @Resource
    private MyService myService;

    @Scheduled(cron = "0 0 9 * * ?")
    public void remindTaskRun() {
        //周生生
        String oneP = "", twoP = "", body = "";
        try {
            body = HttpUtil.get("https://ws.chowsangsang.com/goldpriceapi/goldprice-poss/openapi/v1/list?region=CHN");
            oneP = JSONUtil.parseObj(body).getJSONArray("data").stream().filter(one -> "G_JW_SELL".equals(((JSONObject) one).getStr("type"))).map(one -> ((JSONObject) one).getStr("price")).findFirst().get();
        } catch (Exception ignored) {
        }
        //周大福
        try {
            body = HttpUtil.get("https://api.ctfmall.com/wxmini/api/common/todayGoldPrice?action=gettodayprice");
            twoP = JSONUtil.parseObj(body).getJSONObject("data").getStr("todayPriceHK");
        } catch (Exception ignored) {
        }
        try {
            HttpUtil.get("https://api.day.app/" + myService.getMyConfig().getBarkId() + "/周生生:" + oneP + ";周大福:" + twoP);
        } catch (Exception ignored) {
        }
    }
}