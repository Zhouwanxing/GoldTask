package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.service.*;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class HeartTask {
    @Resource
    private GoldService goldService;
    @Resource
    private UrlService urlService;
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
            HttpUtil.get(Utils.HeartbeatUrl, 3000);
//            log.info();
//            log.info(HttpUtil.get(Utils.HeartbeatNginxUrl, 3000));
//            webSocketServer.sendToAllClient(DateUtil.now());
        } catch (Exception ignored) {

        }
        if (now.getMinute() == 0 && now.getSecond() == 0) {
            if (now.getHour() == 1) {
                urlService.checkNewUrl(true);
            }
            if (now.getHour() > 11) {
                goldService.genToDayGold();
            }
        }
        if (now.getSecond() == 0) {
            if ((now.getHour() == 8 && now.getMinute() > 50) || (now.getHour() == 18 && now.getMinute() < 30 && now.getMinute() > 3)) {
                onlineService.taskOnline();
            }
        }
    }
}