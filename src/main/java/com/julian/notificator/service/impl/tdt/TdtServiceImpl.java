package com.julian.notificator.service.impl.tdt;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.julian.notificator.config.properties.TdtProperties;
import com.julian.notificator.entity.TdtProgrammeEntity;
import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.repository.TdtProgrammeRepository;
import com.julian.notificator.service.TdtService;
import com.julian.notificator.service.util.tdt.UtilTdt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TdtServiceImpl implements TdtService {

    private final TdtProperties tdtProperties;
    private final TdtProgrammeRepository repository;

    @Override
    @Cacheable(value = "tvNow")
    public List<TdtProgramme> getTvNow() {

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withNano(0);

        List<TdtProgramme> result = new ArrayList<>();

        for (String channel : tdtProperties.getNationalChannels()) {

            String normalized = UtilTdt.normalizeChannel(channel);

            List<TdtProgrammeEntity> entities = repository
                    .findByChannelNormalizedAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(normalized, now, now);

            if (entities.isEmpty()) {
                TdtProgramme empty = new TdtProgramme();
                empty.setChannelId(channel);
                empty.setTitle("Sin programación");
                result.add(empty);
            } else {
                result.add(mapToModel(entities.get(0)));
            }
        }

        return result;
    }

    private TdtProgramme mapToModel(TdtProgrammeEntity entity) {
        TdtProgramme p = new TdtProgramme();
        p.setChannelId(entity.getChannelId());
        p.setChannelDesc(entity.getChannelNormalized());
        p.setTitle(entity.getTitle());
        p.setDesc(entity.getDescription());
        p.setStart(entity.getStartTime());
        p.setStop(entity.getEndTime());
        return p;
    }

    private String formatTime(ZonedDateTime time) {
        return time.withZoneSameInstant(ZoneId.of("Europe/Madrid"))
                   .format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replaceAll("([_*\\[\\]()~`>#+\\-=|{}.!])", "\\\\$1");
    }

    @Override
    public String buildTdtMessage(List<TdtProgramme> tvNow) {
        StringBuilder sb = new StringBuilder();

        sb.append("📺 *Programación actual – TV Nacional*\n");
        sb.append("⏰ Ahora mismo en emisión:\n\n");

        for (TdtProgramme programme : tvNow) {
            String channelName = programme.getChannelDesc() != null ? programme.getChannelDesc() : programme.getChannelId();
            sb.append("📺 *").append(escapeMarkdown(channelName)).append("*\n");

            String title = programme.getTitle() != null ? programme.getTitle() : "Sin programación";
            sb.append("_").append(escapeMarkdown(title)).append("_\n");

            if (programme.getStart() != null && programme.getStop() != null) {
                sb.append("🕒 ").append(formatTime(programme.getStart()))
                  .append(" – ").append(formatTime(programme.getStop()))
                  .append("\n");
            }

            sb.append("────────────────\n");
        }

        return sb.toString();
    }
}
