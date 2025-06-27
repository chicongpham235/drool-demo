package com.drools.rule_management.producer;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Service;

import com.drools.rule_management.module.DomainEvent;

@Service
public class MQTTv5ProducerService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MQTTv5ProducerService.class);
    private final MqttClient newClient;

    public MQTTv5ProducerService(MqttClient newClient) {
        this.newClient = newClient;
        newClient.setCallback(new MqttCallback() {
            @Override
            public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
            }

            @Override
            public void mqttErrorOccurred(MqttException e) {
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            }

            @Override
            public void deliveryComplete(IMqttToken iMqttToken) {
            }

            @Override
            public void connectComplete(boolean b, String s) {
                logger.info("[MQTTv5ProducerService] connect: " + b);
            }

            @Override
            public void authPacketArrived(int i, MqttProperties mqttProperties) {
            }
        });
    }

    public void send(DomainEvent event) {
        logger.info("[MQTTv5ProducerService] send:" + event.getCode());
        if (event.getCode() == null) {
            logger.info("[MQTTv5ProducerService] no send");
            return;
        }
        send("rules/" + event.getCode(), new DroolRuleSocketDetail(event));
    }

    public void send(String topic, DroolRuleSocketDetail domain) {
        MqttMessage msg = new MqttMessage(domain.toBytes());
        msg.setQos(1);
        try {
            newClient.publish(topic, msg);
            logger.info("[MQTTv5ProducerService] sended:" + topic);

        } catch (MqttException e) {
            String errorString = e.getMessage();
            logger.info("[MQTTv5ProducerService] send error" + errorString);
        }
    }

}
