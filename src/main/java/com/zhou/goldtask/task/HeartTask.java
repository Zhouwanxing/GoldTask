package com.zhou.goldtask.task;

import cn.hutool.http.HttpUtil;
import com.zhou.goldtask.entity.EnvConfig;
import com.zhou.goldtask.entity.SaTokenMongoData;
import com.zhou.goldtask.service.*;
import com.zhou.goldtask.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Date;

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
    @Resource
    private AJKService ajkService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private Mp4Service mp4Service;
    @Value("${server.port:8080}")
    private int port;

    @Scheduled(cron = "${heartTask.cron:0 * * * * ?}")
    public void remindTaskRun() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getMinute() % 10 == 0) {
            saveServerInfo();
        }
//        log.info("心跳任务,{},{}", now, envConfig.getHostName());
        if (Utils.localhost.equals(envConfig.getHostName())) {
            return;
        }
        try {
            HttpUtil.get("https://" + envConfig.getHostName() + Utils.HeartbeatUrl, 3000);
//            log.info();
//            log.info(HttpUtil.get(Utils.HeartbeatNginxUrl, 3000));
//            webSocketServer.sendToAllClient(DateUtil.now());
        } catch (Exception ignored) {

        }
        if (now.getMinute() == 0 && now.getSecond() == 0) {
            if (now.getHour() == 1) {
                urlService.checkNewUrl(true);
                if (now.getDayOfMonth() == 27) {
                    mp4Service.genNotInIds();
                }
            }
            if (now.getHour() > 11) {
                goldService.genToDayGold();
            }
            if (now.getHour() > 6 && now.getHour() < 20) {
                ajkService.startAjk();
                ajkService.handleFtx();
            }
        }
        if (now.getSecond() == 0) {
            if ((now.getHour() == 8 && now.getMinute() > 50) || (now.getHour() == 18 && now.getMinute() < 30 && now.getMinute() > 3)) {
                onlineService.taskOnline();
            }
        }
    }

    public void saveServerInfo() {
        try {
            SaTokenMongoData data = SaTokenMongoData.builder().id(InetAddress.getLocalHost().getHostName())
                    .key("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + this.port + "/page")
                    .expireAt(new Date(11 * 60 * 1000 + System.currentTimeMillis()))
                    .string("host").build();
            mongoTemplate.save(data);
        } catch (UnknownHostException ignored) {
        }
    }
}