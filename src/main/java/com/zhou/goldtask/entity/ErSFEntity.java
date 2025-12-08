package com.zhou.goldtask.entity;

import cn.hutool.core.date.DateUtil;
import lombok.*;
import org.jsoup.nodes.Element;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_ersf")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ErSFEntity {
    @Id
    private String _id;
    private String title;
    private String info;
    private String priceStr;
    private double price;
    private double area;
    //单价
    private int unitPrice;
    private String lastTime;
    private String createTime;
    private String linkUrl;
    private String from;
    private String floor;
    private Boolean like;


    public void makeOther() {
        from = "ajk";
        if (info != null) {
            String s = info.substring(info.lastIndexOf(" ", info.indexOf("㎡")) + 1, info.indexOf("㎡"));
            this.area = Double.parseDouble(s);
            this.floor = String.valueOf(info.charAt(info.indexOf("层") - 1));
        }
        if (priceStr != null) {
            String s = priceStr.substring(0, priceStr.indexOf(" "));
            this.price = Double.parseDouble(s);
        }
        this.handleUintPrice();
    }

    public void handLJ(Element element) {
        from = "lj";
        try {
            String attr = element.getElementsByClass("noresultRecommend").get(0).attr("href");
            String[] split = attr.split("/");
            _id = split[split.length - 1].split("\\.")[0];
            title = element.getElementsByClass("lj-lazy").get(0).attr("alt");
            info = element.getElementsByClass("houseInfo").text();
            floor = String.valueOf(info.charAt(info.indexOf("楼层") - 1));
            lastTime = DateUtil.now();
            linkUrl = attr;
            priceStr = element.getElementsByClass("priceInfo").get(0).text();
            this.price = Double.parseDouble(element.getElementsByClass("priceInfo").get(0).getElementsByTag("span").get(0).text());
            String[] infoSp = info.split("\\|");
            area = Double.parseDouble(infoSp[1].trim().replace("平米", ""));
            this.handleUintPrice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncLj() {
        from = "lj";
        try {
            floor = String.valueOf(info.charAt(info.indexOf("楼层") - 1));
            lastTime = DateUtil.now();
            String[] sp = priceStr.split("万");
            this.price = Double.parseDouble(sp[0].trim());
            String[] infoSp = info.split("\\|");
            area = Double.parseDouble(infoSp[1].trim().replace("平米", ""));
            this.handleUintPrice();
        } catch (Exception ignored) {
        }
    }

    private void handleUintPrice() {
        if (priceStr != null && priceStr.contains("万") && priceStr.contains("元")) {
            try {
                String replace = priceStr.substring(priceStr.indexOf("万") + 1, priceStr.indexOf("元")).replace(" ", "").replace(",", "");
                this.unitPrice = Integer.parseInt(replace);
            } catch (Exception ignored) {
            }
        }
    }

    public void syncFtx(Element one, String ftxValue) {
        from = "ftx";
        try {
            _id = one.getElementsByClass("floatl").get(0).getElementsByTag("a").get(0).attr("href").split("/")[2].replace(".htm", "");
            title = one.getElementsByTag("h4").text();
            info = one.getElementsByTag("dd").get(0).getElementsByTag("p").get(0).text();
            priceStr = one.getElementsByClass("price_right").get(0).text();
            linkUrl = ftxValue + one.getElementsByClass("floatl").get(0).getElementsByTag("a").get(0).attr("href");
            price = Double.parseDouble(priceStr.substring(0, priceStr.indexOf("万")));
            unitPrice = Integer.parseInt(priceStr.substring(priceStr.indexOf("万") + 1, priceStr.indexOf("元")));
            area = Double.parseDouble(info.substring(info.indexOf("|") + 1, info.indexOf("㎡")).replace(" ", ""));
            floor = info.substring(info.indexOf("层") - 1, info.indexOf("层"));
            lastTime = DateUtil.now();
        } catch (Exception ignored) {

        }
    }

    public static void main(String[] args) {
        String info = "3室2厅 | 120.05平米 | 东南 | 精装 | 中层(共48层) | 板楼";
        System.out.println(info.charAt(info.indexOf("层") - 1));
        ErSFEntity one = ErSFEntity.builder().info(info).priceStr("398万 20,534元/平").build();
        one.handleUintPrice();
        System.out.println(one.getUnitPrice());
    }
}
