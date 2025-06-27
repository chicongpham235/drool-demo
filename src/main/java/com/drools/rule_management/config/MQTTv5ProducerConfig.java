package com.drools.rule_management.config;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.drools.rule_management.base.BaseClass;

import jakarta.annotation.PreDestroy;

@Configuration
public class MQTTv5ProducerConfig extends BaseClass {

    private MqttClient client;

    @Value("${mqtt.connection.string:tcp://localhost:18014}")
    private String broker;
    @Value("${mqtt.clientId:bidv-drools-rule-management}")
    private String prefixClientId;

    public MQTTv5ProducerConfig() {
        super.getInstance(MQTTv5ProducerConfig.class);
    }

    @Bean
    public MqttClient clientInstance() throws MqttException {
        String clientId = prefixClientId + "-" + System.currentTimeMillis();
        logger.info("clientInstance: broker = " + broker + ", clientId=" + clientId);
        try {
            client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectionOptions options = new MqttConnectionOptions();
            options.setConnectionTimeout(30); // Đơn vị: giây
            options.setCleanStart(true);
            client.connect(options);
            logger.info("clientInstance: client = " + client.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    @PreDestroy
    public void disconnect() {
        if (client != null && client.isConnected()) {
            try {
                logger.info("Disconnecting MQTT client...");
                client.disconnect();
            } catch (MqttException e) {
                logger.error("Error while disconnecting MQTT: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Server error exception: " + e.getMessage());
            }
        }
    }
}
