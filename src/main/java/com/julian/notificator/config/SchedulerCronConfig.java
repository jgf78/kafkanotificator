package com.julian.notificator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.julian.notificator.config.properties.DailySchedulerProperties;

@Configuration
public class SchedulerCronConfig {

    private final DailySchedulerProperties props;

    public SchedulerCronConfig(DailySchedulerProperties props) {
        this.props = props;
    }

    @Bean
    String dailyCron() {
        return String.format("0 %d %d * * *", props.getMinute(), props.getHour());
    }

    @Bean
    String dailyZone() {
        return props.getZone();
    }
}
