package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.GoldEntity;
import com.zhou.goldtask.service.GoldService;
import com.zhou.goldtask.service.ITaskService;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class HeartTask {
    @Resource
    private ITaskService taskService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private GoldService goldService;

    @Scheduled(cron = "0/10 * * * * ?")
    public void remindTaskRun() {
        LocalDateTime now = LocalDateTime.now();
        try {
            log.info("{},{},{}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), now.getMinute(), now.getSecond());
            log.info(HttpUtil.get("https://goldtask.onrender.com/"));
        } catch (Exception ignored) {

        }
        if (now.getHour() == 9 && now.getMinute() == 0 && now.getSecond() == 0) {
            goldService.genToDayGold();
        } else if (now.getMinute() == 0 && now.getSecond() == 0) {
            mem2Redis();
        }
        if (now.getHour() == 12 && now.getMinute() == 0 && now.getSecond() == 0) {
            goldTask();
        }
    }

    private void mem2Redis() {
        Long size = redisTemplate.opsForList().size(Utils.goldRedisKey);
        if (size == null || size == 0) {
            List<GoldEntity> list = AllGoldData.getInstance().getList();
            for (GoldEntity goldEntity : list) {
                redisTemplate.opsForList().rightPush(Utils.goldRedisKey, JSONUtil.toJsonStr(goldEntity));
            }
        }
    }

    private void goldTask() {
        taskService.remindTask(LocalDate.now().toString(), "周生生:" + AllGoldData.getInstance().getLast().getZss() + ";周大福:" + AllGoldData.getInstance().getLast().getZdf());
    }
}