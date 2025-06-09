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
    private String lastTime;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String info = "3室2厅 | 120.05平米 | 东南 | 精装 | 中层(共48层) | 板楼";
        System.out.println(info.charAt(info.indexOf("层") - 1));
    }
}
