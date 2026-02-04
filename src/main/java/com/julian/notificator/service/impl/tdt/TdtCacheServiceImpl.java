package com.julian.notificator.service.impl.tdt;

import com.julian.notificator.model.tdt.TdtProgramme;
import com.julian.notificator.service.TdtCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TdtCacheServiceImpl implements TdtCacheService {

    private final RedisTemplate<String, TdtProgramme> redisTemplate;

    private static final String KEY_PREFIX = "tvnow:";

    @Override
    public void cacheNow(TdtProgramme programme) {

        long ttlSeconds = Duration.between(ZonedDateTime.now(programme.getStop().getZone()),
                                           programme.getStop()).getSeconds();

        if (ttlSeconds <= 0) {
            return;
        }

        String key = KEY_PREFIX + programme.getChannelId();

        redisTemplate.opsForValue().set(key, programme, Duration.ofSeconds(ttlSeconds));

        log.debug("📺 Cacheado {} (TTL {}s)", key, ttlSeconds);
    }

    @Override
    public TdtProgramme getCachedNow(String channelId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + channelId);
    }
}
