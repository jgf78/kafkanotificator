package com.julian.notificator.scheduler;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    public DailyScheduler(DailySchedulerProperties props, ApplicationContext context) {
        this.props = props;
        this.context = context;
    }

    @Scheduled(cron = "#{@dailyCron}", zone = "Europe/Madrid")
    public void sendDailyNotification() {

        if (!props.isEnabled()) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
        DayOfWeek dayOfWeek = now.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.info("Fin de semana ({}), no se envía mensaje", dayOfWeek);
            return;
        }

        NotificationService service =
                context.getBean(props.getService(), NotificationService.class);

        String hour = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        int day = now.getDayOfMonth();
        String phrase = PHRASES.get(day - 1);

        String finalMessage = String.format(
                "%s son las %s, que tengas un feliz día 🙂\n\nMotivación del día: %s",
                props.getMessage(), hour, phrase
        );

        log.info("Enviando mensaje diario:\n{}", finalMessage);

        service.sendMessage(finalMessage);
    }

}
