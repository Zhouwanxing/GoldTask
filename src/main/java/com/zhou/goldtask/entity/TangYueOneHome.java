package com.zhou.goldtask.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TangYueOneHome {
    private String _id;
    //价格
    private int price;
    //预测面积
    private double areaPredict;
    //实测面积
    private double areaReal;
    //栋
    private String building;
    //单元
    private String unit;
    //楼层
    private String floor;
    //房号
    private String room;


    public void setAllId(String id) {
        this._id = id;
        try {
            this.building = id.substring(id.indexOf("栋") - 3, id.indexOf("栋"));
            this.unit = id.substring(id.indexOf("栋") + 1, id.indexOf("单元"));
            this.floor = id.substring(id.indexOf("单元") + 2, id.indexOf("层"));
            this.room = id.substring(id.indexOf("层") + 1, id.indexOf("号"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(id);
        }
    }
}