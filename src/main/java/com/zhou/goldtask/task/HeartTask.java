package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.service.MyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class HeartTask {
    @Resource
    private MyService myService;

    @Scheduled(cron = "0/5 * * * * ?")
    public void remindTaskRun() {
        LocalDateTime now = LocalDateTime.now();
        try {
            log.info(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info(HttpUtil.get("https://goldtask.onrender.com/"));
        } catch (Exception ignored) {

        }
        if (now.getMinute() == 0 && now.getSecond() == 0) {
            goldTask();
        }
    }

    private void goldTask() {
        //周生生
        String oneP = "", twoP = "", body = "";
        try {
            body = HttpUtil.get("https://ws.chowsangsang.com/goldpriceapi/goldprice-poss/openapi/v1/list?region=CHN");
            oneP = JSONUtil.parseObj(body).getJSONArray("data").stream().filter(one -> "G_JW_SELL".equals(((JSONObject) one).getStr("type"))).map(one -> ((JSONObject) one).getStr("price")).findFirst().get();
        } catch (Exception e) {
            log.warn("", e);
        }
        //周大福
        try {
            body = HttpUtil.get("https://api.ctfmall.com/wxmini/api/common/todayGoldPrice?action=gettodayprice");
            twoP = JSONUtil.parseObj(body).getJSONObject("data").getStr("todayPriceHK");
        } catch (Exception e) {
            log.warn("", e);
        }
        try {
            HttpUtil.get("https://api.day.app/" + myService.getMyConfig().getBarkId() + "/周生生:" + oneP + ";周大福:" + twoP);
//            log.info("https://api.day.app/" + myService.getMyConfig().getBarkId() + "/周生生:" + oneP + ";周大福:" + twoP);
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}