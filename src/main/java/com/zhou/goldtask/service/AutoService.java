package com.zhou.goldtask.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AttendanceInfo;
import com.zhou.goldtask.utils.MD5;
import lombok.extern.slf4j.Slf4j;
import org.bson.json.JsonObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AutoService {
    @Resource
    private MongoTemplate mongoTemplate;

    public List<Map<String, Object>> getInfo() {
        JSONObject config = attInfo();
        if (config == null) {
            return null;
        }
        try {
            String body = HttpUtil.post(config.getStr("url"), config.getStr("data"), 5000);
            JSONObject result = JSONUtil.parseObj(body);
            if (result.getBool("Succeed", false)) {
                return result.get("Result", AttendanceInfo.class).getRecord();
            }
            log.warn("{}", body);
        } catch (Exception e) {
            log.warn("", e);
        }
        return null;
    }

    public void inWork() {
        JSONObject config = attInfo();
        if (config == null) {
            return;
        }
        String userId = config.getStr("userId");
        String att = config.getStr("att");
        String md5ofStr = new MD5().getMD5ofStr(userId + DateUtil.today() + "hollyc5Sa" + "hlwgd230831");
        JSONObject obj = JSONUtil.parseObj(att);
        obj.putOpt("User", userId);
        obj.putOpt("LoginUser", userId);
        obj.putOpt("Token", md5ofStr);
        obj.putOpt("DeviceId", UUID.randomUUID().toString());
        String url = HttpUtil.post(config.getStr("url"), obj.toString(), 5000);
        System.out.println(url);
    }


    private JSONObject attInfo() {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("GetAttendanceInfo"));
        return mongoTemplate.findOne(query, JSONObject.class, "system_config");
    }
}