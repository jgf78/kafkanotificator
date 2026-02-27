package com.julian.notificator.scheduler;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.julian.notificator.entity.RssIpfsHash;
import com.julian.notificator.service.RssIpfsHashService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RssIpfsScheduler {
    
    @Value("${rss.proxy-url6}")
    private String epgUrl;

    private final RssIpfsHashService service;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void init() {
        log.info("📥 Cargando hashes de inicio de aplicación...");
        updateHashes();
    }

    @Scheduled(cron = "0 0 */3 * * *") // cada 3 horas
    public void updateHashes() {
        try {
            File jsonFile = new File(epgUrl);
            if (!jsonFile.exists()) {
                log.info("Archivo JSON no encontrado: {}", jsonFile.getAbsolutePath());
                return;
            }

            log.info("Comienza la carga de Hashes desde JSON: {}", jsonFile.getAbsolutePath());
            
            JsonNode root = objectMapper.readTree(jsonFile);
            LocalDateTime generated = LocalDateTime.parse(root.get("generated").asText().replace("Z", ""));

            JsonNode hashesNode = root.get("hashes");
            List<RssIpfsHash> hashes = new ArrayList<>();
            
            for (JsonNode node : hashesNode) {
                String title = node.hasNonNull("title") ? node.get("title").asText() : "N/A";
                String group = node.hasNonNull("group") ? node.get("group").asText() : "N/A";
                String hash = node.hasNonNull("hash") ? node.get("hash").asText() : null;

                if (hash != null) {
                    RssIpfsHash entity = RssIpfsHash.builder()
                            .title(title)
                            .group(group)
                            .hash(hash)
                            .generated(generated)
                            .build();
                    hashes.add(entity);
                } else {
                    log.warn("Registro saltado por hash nulo: title='{}', group='{}'", title, group);
                }
            }

            log.info("Total de hashes leídos desde JSON: {}", hashes.size());
            for (RssIpfsHash h : hashes) {
                log.info("Hash a guardar: title='{}', group='{}', hash='{}', generated='{}'",
                         h.getTitle(), h.getGroup(), h.getHash(), h.getGenerated());
            }

            if (!hashes.isEmpty()) {
                service.refreshHashes(hashes);
                log.info("Hashes actualizados correctamente en DB a {}", LocalDateTime.now());
            } else {
                log.warn("No hay hashes válidos para guardar en DB");
            }

        } catch (Exception e) {
            log.error("Error actualizando hashes", e);
        }
    }
}