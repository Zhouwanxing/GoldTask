package com.zhou.goldtask.service;

public interface ITaskService {
    void remindTask(String title, String content,String group, boolean isAutoSave);
}
