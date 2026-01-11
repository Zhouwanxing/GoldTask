package com.zhou.goldtask.entity;

import lombok.*;
import org.jsoup.nodes.Element;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_chengjiao")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CJEntity {
    @Id
    private String _id;
    private String title;
    private String info;
    private double price;
    private double area;
    //单价
    private int unitPrice;
    private String createTime;
    private String linkUrl;
    private String from;
    private String floor;
    //销售
    private String sale;

    public boolean parseCJ(Element one) {
        try {
            String attr = one.getElementsByClass("img").get(0).attr("href");
            this.linkUrl = attr;
            String[] split = attr.split("/");
            String s = split[split.length - 1];
            this._id = s.substring(0, s.indexOf("."));
            this.title = one.getElementsByClass("title").get(0).text();
            this.info = one.getElementsByClass("positionInfo").text();
            this.createTime = one.getElementsByClass("dealDate").text();
            this.sale = one.getElementsByClass("agent_name").text();
            this.area = Double.parseDouble(title.split(" ")[2].replace("平米", ""));
            this.from = "lj";
            this.floor = info.substring(0, 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void handlePrice(double inPrice) {
        this.price = inPrice;
        this.unitPrice = (int) (inPrice * 10000 / this.area);
    }
}