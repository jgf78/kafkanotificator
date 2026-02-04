package com.julian.notificator.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "scheduler.daily")
public class DailySchedulerProperties {
    private boolean enabled;
    private String message;
    private int hour;
    private int minute;
    private String zone;
    private String service;
}
