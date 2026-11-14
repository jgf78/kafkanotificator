package com.julian.notificator.scheduler;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.julian.notificator.config.properties.DailySchedulerProperties;
import com.julian.notificator.model.calendarofsaints.CalendarOfSaintsResponse;
import com.julian.notificator.model.telegram.DestinationTelegramType;
import com.julian.notificator.service.CalendarOfSaintsService;
import com.julian.notificator.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DailyScheduler {

    private static final ZoneId MADRID_ZONE = ZoneId.of("Europe/Madrid");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final EnumSet<DayOfWeek> WEEKEND = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final DailySchedulerProperties props;
    private final ApplicationContext context;
    private final CalendarOfSaintsService calendarOfSaintsService;

    private static final List<String> PHRASES = List.of(
        "Hoy es un gran día para empezar algo nuevo.",
        "Cree en ti, incluso cuando nadie más lo haga.",
        "Pequeños pasos también te llevan lejos.",
        "Tu actitud determina tu altitud.",
        "Haz de hoy un día inolvidable.",
        "Nunca es tarde para ser la mejor versión de ti.",
        "La constancia vence al talento cuando el talento no se esfuerza.",
        "Hoy tienes la oportunidad de cambiar tu vida.",
        "Lo mejor está por venir.",
        "Cada día cuenta. Aprovecha este.",
        "Rodéate de lo que te inspire.",
        "Haz lo que puedas, con lo que tengas, donde estés.",
        "El éxito empieza con un buen hábito.",
        "Brilla sin miedo.",
        "Eres capaz de cosas increíbles.",
        "No cuentes los días, haz que los días cuenten.",
        "Tu futuro lo decides hoy.",
        "Deja que la energía positiva te guíe.",
        "No te rindas, lo mejor llega cuando menos lo esperas.",
        "Hoy será un día espectacular.",
        "El primer paso es siempre el más difícil, pero el más importante.",
        "Tu esfuerzo de hoy es tu éxito de mañana.",
        "Haz algo hoy que tu yo del futuro agradezca.",
        "Respira profundo, todo irá bien.",
        "Rodéate de motivación, avanza sin miedo.",
        "Cada amanecer trae una oportunidad nueva.",
        "Sé amable contigo mismo.",
        "Si puedes soñarlo, puedes hacerlo.",
        "Hoy es el momento perfecto para empezar.",
        "Tu mejor versión te está esperando.",
        "Convierte este día en una victoria."
    );

    public DailyScheduler(
            DailySchedulerProperties props,
            ApplicationContext context,
            CalendarOfSaintsService calendarOfSaintsService) {
        this.props = props;
        this.context = context;
        this.calendarOfSaintsService = calendarOfSaintsService;
    }

    @Scheduled(cron = "#{@dailyCron}", zone = "Europe/Madrid")
    public void sendDailyNotification() {

        if (!props.isEnabled()) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(MADRID_ZONE);
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        if (WEEKEND.contains(dayOfWeek)) {
            log.info("Fin de semana ({}), no se envía mensaje", dayOfWeek);
            return;
        }

        NotificationService service =
                context.getBean(props.getService(), NotificationService.class);

        String hour = now.format(TIME_FORMATTER);

        int day = now.getDayOfMonth();
        int phraseIndex = Math.floorMod(day - 1, PHRASES.size());
        String phrase = PHRASES.get(phraseIndex);

        StringBuilder message = new StringBuilder(
                String.format(
                        "%s son las %s, que tengas un feliz día 🙂%n%nMotivación del día: %s",
                        props.getMessage(), hour, phrase
                )
        );

        // Consultar el santoral de España
        try {
            CalendarOfSaintsResponse saintsResponse = calendarOfSaintsService.getSaints();

            if (saintsResponse != null
                    && saintsResponse.success()
                    && saintsResponse.saints() != null
                    && !saintsResponse.saints().isBlank()) {

                String saints = saintsResponse.saints().trim();
                saints = stripTrailingPunctuation(saints);

                message.append(String.format(
                        "%n%nEl santoral de hoy es para: %s.",
                        saints
                ));
            }

        } catch (RuntimeException e) {
            log.warn("No se ha podido consultar el santoral de hoy: {}", e.getMessage());
        }

        String finalMessage = message.toString();

        log.info("Enviando mensaje diario:\n{}", finalMessage);

        service.sendMessage(finalMessage, DestinationTelegramType.GROUPS);
    }

    private static String stripTrailingPunctuation(String value) {
        int end = value.length();

        while (end > 0 && (value.charAt(end - 1) == '.' || value.charAt(end - 1) == ',')) {
            end--;
        }

        return value.substring(0, end);
    }

}