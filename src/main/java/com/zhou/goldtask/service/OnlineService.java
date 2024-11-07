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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OnlineService {
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private ITaskService taskService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void taskOnline() {
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("online"))) {
            return;
        }
        AttendanceInfo info = getAttendanceInfo();
        if (info == null || !info.isWorkDay()) {
            return;
        }
        if (DateUtil.thisHour(true) < 9) {
            if (info.getRecord().size() == 0) {
                taskService.remindTask("打卡", "上班打卡", false);
            } else {
                stringRedisTemplate.opsForValue().set("online", "1", 30, TimeUnit.MINUTES);
            }
        } else {
            if (info.getRecord().size() != 2) {
                taskService.remindTask("打卡", "下班打卡", false);
            } else {
                stringRedisTemplate.opsForValue().set("online", "1", 30, TimeUnit.MINUTES);
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