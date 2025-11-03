package com.julian.notificator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic discordTopic() {
        return new NewTopic("discord-messages", 1, (short) 1);
    }
}
