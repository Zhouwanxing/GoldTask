package com.zhou.goldtask.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.controller.WebSocketServer;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.service.*;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class HeartTask {
    @Resource
    private ITaskService taskService;
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
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private WebSocketServer webSocketServer;

    @Scheduled(cron = "${heartTask.cron:0 * * * * ?}")
    public void remindTaskRun() {
        if (Utils.localhost.equals(envConfig.getHostName())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        try {
            HttpUtil.get(Utils.HeartbeatUrl, 3000);
//            log.info();
//            log.info(HttpUtil.get(Utils.HeartbeatNginxUrl, 3000));
            webSocketServer.sendToAllClient(DateUtil.now());
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
            }
        }
        if (now.getSecond() == 0) {
            if ((now.getHour() == 8 && now.getMinute() > 50) || (now.getHour() == 18 && now.getMinute() < 30 && now.getMinute() > 3)) {
                onlineService.taskOnline();
            }
        }
    }

    private String getMongoUse() {
        try {
            Document document = mongoTemplate.executeCommand("{ dbStats: 1 }");
            long totalSize = document.getLong("dataSize") + document.getLong("indexSize");
            double mb = (double) totalSize / 1024 / 1024;
            DecimalFormat decimalFormat = new DecimalFormat("#.00");
            return mb > 1 ? decimalFormat.format(mb) + "MB" : decimalFormat.format((double) totalSize / 1024) + "KB";
        } catch (Exception e) {
            return "";
        }
    }

    private void goldTask() {
        String content = "周生生:" + goldService.getTodayGold().getZss() + ";周大福:" + goldService.getTodayGold().getZdf() + ";占用:" + getMongoUse();
        taskService.remindTask(LocalDate.now().toString(), content, true);
    }
}