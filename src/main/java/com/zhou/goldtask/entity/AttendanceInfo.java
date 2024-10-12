package com.zhou.goldtask.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AttendanceInfo {
    private List<Map<String, Object>> record;
    private boolean isWorkDay = false;
    private String title;
}