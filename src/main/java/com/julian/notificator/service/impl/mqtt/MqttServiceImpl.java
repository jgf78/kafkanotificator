package com.julian.notificator.service.impl.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.julian.notificator.model.MessagePayload;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("mqttServiceImpl")
public class MqttServiceImpl implements NotificationService {

    @Value("${mqtt.broker}")
    private String broker;

    @Value("${mqtt.topic}")
    private String topic;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.qos}")
    private int qos;

    @Override
    public void sendMessage(String message) {
        try (IMqttClient client = new MqttClient(broker, clientId)) {

            client.connect();

            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(qos);            
            msg.setRetained(false);

            client.publish(topic, msg);

            log.info("MQTT enviado a topic {}: {}", topic, message);

            client.disconnect();

        } catch (MqttException e) {
            log.error("Error enviando MQTT", e);
        }
    }

    @Override
    public String getChannelName() {
        return "MQTT";
    }

    @Override
    public void sendMessageFile(MessagePayload payload) {
        // TODO Auto-generated method stub
        
    }
}
