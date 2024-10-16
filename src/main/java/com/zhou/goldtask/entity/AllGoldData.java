package com.zhou.goldtask.entity;

import java.util.ArrayList;
import java.util.List;

public class AllGoldData {
    private static AllGoldData instance;
    private final List<String> urls = new ArrayList<>();

    private AllGoldData() {
    }

    public static synchronized AllGoldData getInstance() {
        if (instance == null) {
            instance = new AllGoldData();
        }
        return instance;
    }

    public List<String> getUrls() {
        return urls;
    }

    public boolean addUrl(String url) {
        if (urls.stream().noneMatch(one -> one.equals(url))) {
            urls.add(url);
            return true;
        }
        return false;
    }

    public void removeUrl(String url) {
        urls.remove(url);
    }
}