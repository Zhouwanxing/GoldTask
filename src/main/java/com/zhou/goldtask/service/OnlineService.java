package com.zhou.goldtask.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AttendanceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class OnlineService {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ITaskService taskService;

    public void taskOnline() {
        AttendanceInfo info = getAttendanceInfo();
        if (info == null || !info.isWorkDay()) {
            return;
        }
        if (DateUtil.thisHour(true) < 9) {
            if (info.getRecord().size() == 0) {
                taskService.remindTask("打卡", "上班打卡");
            }
        } else {
            if (info.getRecord().size() != 2) {
                taskService.remindTask("打卡", "下班打卡");
            }
        }
    }

    private AttendanceInfo getAttendanceInfo() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("GetAttendanceInfo"));
        JSONObject config = mongoTemplate.findOne(query, JSONObject.class, "system_config");
        if (config == null) {
            return null;
        }
        try {
            String body = HttpUtil.post(config.getStr("url"), config.getStr("data"), 5000);
            JSONObject result = JSONUtil.parseObj(body);
            if (result.getBool("Succeed", false)) {
                return result.get("Result", AttendanceInfo.class);
            }
            log.warn("{}", body);
        } catch (Exception e) {
            log.warn("", e);
        }
        return null;
    }
}