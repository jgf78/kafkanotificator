package com.julian.notificator.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EpgDownloadService {

    private static final String EPG_URL = "https://www.tdtchannels.com/epg/TV.xml.gz";

    private InputStream lastEpgStream; 
    
    @PostConstruct
    public void init() {
        downloadEpg(); // descarga al arrancar la app
    }

    @Scheduled(cron = "0 0 */2 * * *") // cada 2 horas
    //@Scheduled(fixedRate = 10000) // cada 10 segundos
    public void downloadEpg() {
        log.info("📥 Descargando EPG desde {}", EPG_URL);

        try {
            InputStream gzStream = new URL(EPG_URL).openStream();
            GZIPInputStream xmlStream = new GZIPInputStream(gzStream);

            lastEpgStream = xmlStream;

            log.info("✅ EPG descargada y descomprimida correctamente");

        } catch (IOException e) {
            log.error("❌ Error descargando o descomprimiendo la EPG", e);
        }
    }

    public InputStream getLastEpgStream() {
        return lastEpgStream;
    }
}

