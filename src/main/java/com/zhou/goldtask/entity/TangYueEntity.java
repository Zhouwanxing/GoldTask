package com.zhou.goldtask.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@ToString
@Document("my_tangyue")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TangYueEntity {
    @Id
    private String _id;
    private int price;
    //预测面积
    private double areaPredict;
    //实测面积
    private double areaReal;
    //栋
    private String building = "";
    //单元
    private String unit = "";
    //楼层
    private String floor = "";
    //房号
    private String room = "";
    //小区
    private String xq = "";
}