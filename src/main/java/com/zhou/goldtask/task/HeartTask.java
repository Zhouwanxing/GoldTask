package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.AllGoldData;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.service.*;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Resource
    private UrlService urlService;
    @Resource
    private Mp4Service mp4Service;
    @Resource
    private OnlineService onlineService;
    @Resource
    private EnvConfig envConfig;

    @Scheduled(cron = "${heartTask.cron:0 * * * * ?}")
    public void remindTaskRun() {
        if (Utils.localhost.equals(envConfig.getHostName())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        try {
            log.info(HttpUtil.get(Utils.HeartbeatUrl, 3000));
            log.info(HttpUtil.get(Utils.HeartbeatNginxUrl, 3000));
        } catch (Exception ignored) {

        }
        if (now.getMinute() == 0 && now.getSecond() == 0) {
            if (now.getHour() % 12 == 1) {
                urlService.checkNewUrl();
            } else if (now.getHour() % 12 == 2) {
                mp4Service.genNew();
            } else if (now.getHour() % 12 == 10) {
                goldService.genToDayGold();
            } else if (now.getHour() == 12) {
                goldTask();
            } else {
                mem2Redis();
            }
        }
        if (now.getSecond() == 0) {
            if ((now.getHour() == 8 && now.getMinute() > 50) || (now.getHour() == 18 && now.getMinute() < 30 && now.getMinute() > 3)) {
                onlineService.taskOnline();
            }
        }
    }

    private void mem2Redis() {
        Long size = redisTemplate.opsForList().size(Utils.UrlRedisKey);
        if (size == null || size == 0) {
            List<String> list = AllGoldData.getInstance().getUrls();
            for (String url : list) {
                redisTemplate.opsForList().rightPush(Utils.UrlRedisKey, url);
            }
        }
    }

    private void goldTask() {
        taskService.remindTask(LocalDate.now().toString(), "周生生:" + goldService.getTodayGold().getZss() + ";周大福:" + goldService.getTodayGold().getZdf());
    }
}