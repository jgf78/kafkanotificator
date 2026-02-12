package com.julian.notificator.service.impl.tdt;


import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.julian.notificator.entity.TdtProgrammeEntity;
import com.julian.notificator.repository.TdtProgrammeRepository;

import lombok.RequiredArgsConstructor;

@CacheEvict(value = "tvNow", allEntries = true)
@Service
@RequiredArgsConstructor
public class EpgPersistService {

    private final TdtProgrammeRepository repository;

    @Transactional
    public void save(List<TdtProgrammeEntity> programmes) {

        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        repository.deleteByEndTimeBefore(now);

        if (!programmes.isEmpty()) {
            repository.saveAll(programmes);
        }
    }

}
