package com.zhou.goldtask.service;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.zhou.goldtask.entity.ErSFEntity;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.repository.GoldRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.ssssssss.magicapi.core.service.MagicAPIService;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GoldService {
    @Resource
    private GoldRepository goldRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MongoTemplate secondMongoTemplate;
    @Resource
    private ITaskService taskService;
    @Resource
    private MagicAPIService magicAPIService;

    public void genToDayGold() {
        String now = LocalDate.now().toString();
        GoldEntity item = goldRepository.findItemById(now);
        if (item != null && now.equals(item.get_id())) {
            /*if (item.getCcb() == 0) {
                setOther(now);
            }*/
            return;
        }
        //周生生
        String oneP = "", twoP = "", body = "";
        GoldEntity gold = GoldEntity.builder()._id(now).build();
        try {
            body = HttpUtil.get("https://ws.chowsangsang.com/goldpriceapi/goldprice-poss/openapi/v1/list?region=CHN",5000);
            oneP = JSONUtil.parseObj(body).getJSONArray("data").stream().filter(one -> "G_JW_SELL".equals(((JSONObject) one).getStr("type"))).map(one -> ((JSONObject) one).getStr("price")).findFirst().get();
            gold.setZss((int) Double.parseDouble(oneP));
        } catch (Exception e) {
            log.warn("", e);
        }
        //周大福
        try {
            body = HttpUtil.get("https://api.ctfmall.com/wxmini/api/common/todayGoldPrice?action=gettodayprice",5000);
            twoP = JSONUtil.parseObj(body).getJSONObject("data").getStr("todayPriceHK");
            gold.setZdf((int) Double.parseDouble(twoP));
        } catch (Exception e) {
            log.warn("", e);
        }
        log.info("{}", gold.toString());
//        int ccb = getOther();
        gold.setCcb(getCommonGold());
        goldRepository.save(gold);
//        setOther(now);
        /*taskService.remindTask(now, "周生生:" + gold.getZss() + ";周大福:" + gold.getZdf() + ";黄金延期:" + ccb
                + ";占用1:" + getMongoUse(mongoTemplate) + ";占用2:" + getMongoUse(secondMongoTemplate), true); */
        taskService.remindTask(now, String.format("周生生:%s;周大福:%s;黄金延期:%s;占用1:%s;占用2:%s,均价:%s",
                gold.getZss(), gold.getZdf(), gold.getCcb(), getMongoUse(mongoTemplate), getMongoUse(secondMongoTemplate), getUnit()), "jj", null, true);
    }

    private int getCommonGold() {
        try {
            String num = magicAPIService.invoke("/gold/ccb", new HashMap<>());
            if (num.contains(".")) {
                num = num.substring(0, num.indexOf("."));
            }
            return Integer.parseInt(num);
        } catch (Exception ignored) {
            return 0;
        }
    }


    private int getUnit() {
        Query query = new Query();
        query.addCriteria(Criteria.where("area").gte(100).lt(130));
        query.fields().include("unitPrice").exclude("_id");
        List<ErSFEntity> list = secondMongoTemplate.find(query, ErSFEntity.class);
        int all = 0;
        for (ErSFEntity one : list) {
            all += one.getUnitPrice();
        }
        return all / list.size();
    }

    public int getCcb() {
        try (WebClient webClient = new WebClient()) {
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setTimeout(5000);
            HtmlPage page = webClient.getPage("https://gold2.ccb.com/chn/home/gold_new/cpjs/index.shtml");
            webClient.waitForBackgroundJavaScript(5000);
            webClient.getCache().clear();
            page = webClient.getPage("https://gold2.ccb.com/tran/WCCMainPlatV5?CCB_IBSVersion=V5&SERVLET_NAME=WCCMainPlatV5&TXCODE=NGJS01");
            return JSONUtil.parseObj(page.getBody().getFirstChild().toString()).getInt("Cst_Buy_Prc");
        } catch (Exception ignored) {
        }
        return 0;
    }

    private void setOther(String now){
        try {
            String body = HttpRequest.get("https://api.goldprice.fun/brandsApiUrl").timeout(5000).header("origin", "https://goldprice.fun").execute().body();
            JSONArray gn = JSONUtil.parseObj(body).getJSONArray("brand");
            JSONObject object = null;
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(now));
            Update update = new Update();
            for (int i = 0; i < gn.size(); i++) {
                object = gn.getJSONObject(i);
                update.set(object.getStr("title"), object.getInt("gold"));
            }
            int ccb = getOther();
            if (ccb != 0) {
                update.set("ccb", ccb);
            }
            mongoTemplate.updateFirst(query, update, GoldEntity.class);
        } catch (Exception ignored) {

        }
    }

    public int getOther() {
        String body = null;
        int price = 0;
        JSONObject object = null;
        JSONArray gn = null;
        try {
            body = HttpRequest.get("https://api.goldprice.fun/domesticGoldApiUrl").timeout(5000).header("origin", "https://goldprice.fun").execute().body();
            gn = JSONUtil.parseObj(body).getJSONArray("gn");
            for (int i = 0; i < gn.size(); i++) {
                object = gn.getJSONObject(i);
                if ("daygold".equals(object.get("dir"))) {
                    price = object.getInt("price");
                }
            }
        } catch (Exception ignored) {

        }
        return price;
    }

    public int getCcbNew() {
        HttpResponse execute = HttpRequest.get("https://gold2.ccb.com/chn/home/gold_new/cpjs/index.shtml").timeout(5000).execute();
        Map<String, List<String>> headers = execute.headers();
        for (String s : headers.keySet()) {
            System.out.println(s + "=" + headers.get(s));
        }
        String body = execute.body();
        System.out.println(body);
        return 0;
    }

    private String getMongoUse(MongoTemplate t) {
        try {
            Document document = t.executeCommand("{ dbStats: 1 }");
            long totalSize = document.getLong("dataSize") + document.getLong("indexSize");
            double mb = (double) totalSize / 1024 / 1024;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return mb > 1 ? decimalFormat.format(mb) + "MB" : decimalFormat.format((double) totalSize / 1024) + "KB";
        } catch (Exception e) {
            return "";
        }
    }
}