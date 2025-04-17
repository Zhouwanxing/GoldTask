package com.zhou.goldtask.service;

import com.zhou.goldtask.entity.SwingConfig;
import com.zhou.goldtask.entity.UserEntity;
import com.zhou.goldtask.repository.UserRepository;
import com.zhou.goldtask.utils.RuntimeData;
import com.zhou.goldtask.utils.aes.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class UserService {
    @Resource
    private UserRepository userRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private MongoTemplate secondMongoTemplate;

    public void mongoTest() {
        //数据库心跳检测
        mongoTemplate.executeCommand("{ ping: 1 }");
        secondMongoTemplate.executeCommand("{ ping: 1 }");
    }


    public SwingConfig getSwingConfig() {
        SwingConfig mConfig = RuntimeData.getInstance().getSwingConfig();
        if (mConfig != null) {
            return mConfig;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is("swingConfig"));
        SwingConfig config = mongoTemplate.findOne(query, SwingConfig.class, "system_config");
        if (config == null) {
            return new SwingConfig();
        }
        RuntimeData.getInstance().setSwingConfig(config);
        return config;
    }


    public boolean isExistUser(String userName, String password) {
        UserEntity user = userRepository.findById(userName).orElse(null);
        return user != null && user.getPassword().equals(password);
    }

    public void sendToMqtt(String messageStr, int qos) {
        SwingConfig config = getSwingConfig();
        if (config == null) {
            return;
        }
        try {
            MqttClient mqttClient = new MqttClient(config.getBroker(), MqttClient.generateClientId(), new MemoryPersistence());
            mqttClient.connect();
            MqttMessage message = new MqttMessage(AESUtil.encryptByMyKey(messageStr, config.getMessageSecret()).getBytes());
            message.setQos(qos);
            mqttClient.publish(config.getTopic(), message);
            mqttClient.disconnect();
            mqttClient.close();
        } catch (Exception e) {
            log.warn("", e);
        }
    }
}