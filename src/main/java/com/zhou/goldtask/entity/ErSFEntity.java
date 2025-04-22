package com.zhou.goldtask.entity;

import lombok.*;
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


    public void makeOther() {
        if (info != null) {
            String s = info.substring(info.lastIndexOf(" ", info.indexOf("㎡")) + 1, info.indexOf("㎡"));
            this.area = Double.parseDouble(s);
        }
        if (priceStr != null) {
            String s = priceStr.substring(0, priceStr.indexOf(" "));
            this.price = Double.parseDouble(s);
        }
    }
}
