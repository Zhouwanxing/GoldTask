package com.zhou.goldtask.service;

import com.zhou.goldtask.controller.WebSocketServer;
import com.zhou.goldtask.entity.MarbleBankEntity;
import com.zhou.goldtask.repository.MarbleBankRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 弹珠获胜概率：全局一条配置，和库存同表（marble_bank 里 id = __config__ 的行）。
 * 管理页改完立刻广播给在线设备；客户端按百分比决定亮灯轨道数，比如 30 就是十条轨道亮三条。
 */
@Service
public class MarbleConfigService {
    public static final int DEFAULT_WIN_CHANCE = 30;
    public static final int MAX_WIN_CHANCE = 100;
    /** 配置行在 marble_bank 集合里的固定 id，不可能撞设备 UUID。 */
    public static final String CONFIG_ID = "__config__";

    @Resource
    private MarbleBankRepository marbleBankRepository;

    public int getWinChance() {
        MarbleBankEntity entity = marbleBankRepository.findById(CONFIG_ID).orElse(null);
        return entity != null && entity.getWinChance() != null ? entity.getWinChance() : DEFAULT_WIN_CHANCE;
    }

    /** 保存新概率并广播。返回落库的值，方便管理页直接采用。 */
    public int saveWinChance(Integer winChance) {
        if (winChance == null || winChance < 0 || winChance > MAX_WIN_CHANCE) {
            throw new IllegalArgumentException("获胜概率要在 0 到 100 之间");
        }
        MarbleBankEntity entity = marbleBankRepository.findById(CONFIG_ID).orElse(null);
        if (entity == null) {
            entity = new MarbleBankEntity();
            entity.setId(CONFIG_ID);
            entity.setName("全局配置");
        }
        entity.setWinChance(winChance);
        entity.setUpdatedAt(System.currentTimeMillis());
        marbleBankRepository.save(entity);
        WebSocketServer.pushMarbleConfig(winChance);
        return winChance;
    }
}
