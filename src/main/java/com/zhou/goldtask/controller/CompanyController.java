package com.zhou.goldtask.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.date.DateUtil;
import com.zhou.goldtask.entity.OnePerson;
import com.zhou.goldtask.entity.TangYueEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@Slf4j
@RestController
@RequestMapping("/page/company")
@CrossOrigin
@SaCheckLogin
@SaCheckRole("company")
public class CompanyController {
    @Resource
    private MongoTemplate mongoTemplate;

    @PostMapping("/saveOne")
    public SaResult saveOne(@RequestBody OnePerson data) {
        SaResult ok = SaResult.ok();
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(DateUtil.today()));
        Update update = new Update();
        update.set(data.getName(), data.getDress());
        mongoTemplate.upsert(query, update, "my_company");
        return ok;
    }
}