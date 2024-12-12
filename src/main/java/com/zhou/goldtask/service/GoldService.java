package com.zhou.goldtask.service;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
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

    public void genToDayGold() {
        String now = LocalDate.now().toString();
        GoldEntity item = goldRepository.findItemById(now);
        if (item != null && now.equals(item.get_id())) {
            return;
        }
        //周生生
        String oneP = "", twoP = "", body = "";
        GoldEntity gold = GoldEntity.builder()._id(now).build();
        try {
            body = HttpUtil.get("https://ws.chowsangsang.com/goldpriceapi/goldprice-poss/openapi/v1/list?region=CHN");
            oneP = JSONUtil.parseObj(body).getJSONArray("data").stream().filter(one -> "G_JW_SELL".equals(((JSONObject) one).getStr("type"))).map(one -> ((JSONObject) one).getStr("price")).findFirst().get();
            gold.setZss((int) Double.parseDouble(oneP));
        } catch (Exception e) {
            log.warn("", e);
        }
        //周大福
        try {
            body = HttpUtil.get("https://api.ctfmall.com/wxmini/api/common/todayGoldPrice?action=gettodayprice");
            twoP = JSONUtil.parseObj(body).getJSONObject("data").getStr("todayPriceHK");
            gold.setZdf((int) Double.parseDouble(twoP));
        } catch (Exception e) {
            log.warn("", e);
        }
        log.info("{}", gold.toString());
        gold.setCcb(getCcb());
        goldRepository.save(gold);
        taskService.remindTask(now, "周生生:" + gold.getZss() + ";周大福:" + gold.getZdf() + ";建设银行:" + gold.getCcb() + ";占用:" + getMongoUse(), true);
    }

    public int getCcb() {
        try {
            WebClient webClient = new WebClient();
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setTimeout(5000);
            HtmlPage page = webClient.getPage("https://gold2.ccb.com/chn/home/gold_new/cpjs/index.shtml");
            webClient.waitForBackgroundJavaScript(5000);
            page = webClient.getPage("https://gold2.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=NGJS01");
            return JSONUtil.parseObj(page.getBody().getFirstChild().toString()).getInt("Cst_Buy_Prc");
        } catch (Exception ignored) {
        }
        return 0;
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