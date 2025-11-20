package com.julian.notificator.scheduler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.config.DailySchedulerProperties;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DailyScheduler {

    private final DailySchedulerProperties props;
    private final ApplicationContext context;

    public DailyScheduler(DailySchedulerProperties props, ApplicationContext context) {
        this.props = props;
        this.context = context;
    }

    @Scheduled(cron = "#{@dailyCron}", zone = "#{@dailyZone}")
    public void sendDailyNotification() {
        if (!props.isEnabled()) {
            return;
        }

        NotificationService service =
                context.getBean(props.getService(), NotificationService.class);

        String now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        String finalMessage = String.format("%sson las %s, que tengas un feliz día 🙂",
                props.getMessage(), now);


        log.info("Enviando mensaje diario:\n{}", finalMessage);

        service.sendMessage(finalMessage);
    }
}
