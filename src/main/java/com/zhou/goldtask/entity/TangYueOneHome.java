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
    private String building = "";
    //单元
    private String unit = "";
    //楼层
    private String floor = "";
    //房号
    private String room = "";


    public void setAllId(String id) {
        this._id = id;
        try {
            if (id.contains("号楼")) {
                this.building = id.substring(id.lastIndexOf("号楼") - 1, id.lastIndexOf("号楼"));
                if(id.contains("号楼 栋")){
                    this.unit = id.substring(id.indexOf("号楼 栋") + 4, id.indexOf("单元"));
                } else {
                    this.unit = id.contains("号楼单元") ? "" : id.substring(id.indexOf("楼栋") + 2, id.indexOf("单元"));
                }
                //1号楼单元25层1号
                //8号楼栋/单元22层5号
                //1号楼栋商业1单元1-2层商2号
            } else {
                this.building = id.substring(id.indexOf("栋") - 3, id.indexOf("栋"));
                this.unit = id.substring(id.indexOf("栋") + 1, id.indexOf("单元"));
            }
            this.floor = id.substring(id.indexOf("单元") + 2, id.indexOf("层"));
            this.room = id.substring(id.indexOf("层") + 1, id.lastIndexOf("号"));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(id);
        }
    }

    public static void main(String[] args) {
        TangYueOneHome home = TangYueOneHome.builder().build();
        home.setAllId("万科唐家墩城中村改造K5地块3、4号楼及商业2号楼 栋/单元1-2层商2号");
        System.out.println(home);
    }
}