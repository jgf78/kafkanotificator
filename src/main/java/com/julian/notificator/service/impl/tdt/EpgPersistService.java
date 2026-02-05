package com.julian.notificator.service.impl.tdt;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.julian.notificator.entity.TdtProgrammeEntity;
import com.julian.notificator.repository.TdtProgrammeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EpgPersistService {

    private final TdtProgrammeRepository repository;

    @Transactional
    public void save(List<TdtProgrammeEntity> programmes, List<String> channelsNormalized) {

        if (!channelsNormalized.isEmpty()) {
            repository.deleteAllByChannelNormalizedIn(channelsNormalized);
        }

        if (!programmes.isEmpty()) {
            repository.saveAll(programmes);
        }
    }
}
