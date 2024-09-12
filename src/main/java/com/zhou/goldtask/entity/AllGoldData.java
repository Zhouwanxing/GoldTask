package com.zhou.goldtask.entity;

import java.util.ArrayList;
import java.util.List;

public class AllGoldData {
    private static AllGoldData instance;
    private final List<GoldEntity> list = new ArrayList<>();

    private AllGoldData() {
    }

    public static synchronized AllGoldData getInstance() {
        if (instance == null) {
            instance = new AllGoldData();
        }
        return instance;
    }

    public List<GoldEntity> getList() {
        return list;
    }

    public void add(GoldEntity goldEntity) {
        if (list.stream().noneMatch(one -> one.getDate().equals(goldEntity.getDate()))) {
            list.add(goldEntity);
        }
    }

    public GoldEntity getLast() {
        if (list.size() == 0) {
            return new GoldEntity("", 0, 0);
        }
        return list.get(list.size() - 1);
    }
}