package com.zhou.goldtask.service;

import cn.hutool.json.JSONObject;
import com.zhou.goldtask.entity.ChromeSession;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.Hashtable;

@Service
@Slf4j
public class ChromeService {
    private final Hashtable<String, ChromeSession> chromeSessions = new Hashtable<>();

    public JSONObject getState(String devId) {
        ChromeSession session = null;
        synchronized (chromeSessions) {
            session = chromeSessions.get(devId);
            if (session == null) {
                session = new ChromeSession(devId);
                this.addSession(devId, session);
            }
        }
        return session.parsePacket();
    }

    public void addSession(String devId, ChromeSession chromeSession) {
        chromeSessions.put(devId, chromeSession);
    }

    public void toChromeCode(String devId, String code) {
        ChromeSession chromeSession = chromeSessions.get(devId);
        if (chromeSession != null) {
            chromeSession.notifyEvent(new JSONObject().set("type", "codeToHtml").set("code", code));
        }
    }

    public void runChromeUrl(String url) {
        System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        System.setProperty("webdriver.chrome.silentOutput", "true");
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        // 设置 ChromeOptions 以启用无界面模式
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 启用无界面模式
        options.addArguments("--disable-gpu"); // 禁用GPU硬件加速
        options.addArguments("--window-size=1920,1080"); // 设置窗口大小
        // 创建 WebDriver 实例，并传入配置
        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);
            log.info("{}", driver.getPageSource());
        } finally {
            // 关闭浏览器
            driver.quit();
        }
    }
}