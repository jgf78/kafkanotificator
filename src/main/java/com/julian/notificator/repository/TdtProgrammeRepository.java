package com.julian.notificator.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julian.notificator.entity.TdtProgrammeEntity;

public interface TdtProgrammeRepository extends JpaRepository<TdtProgrammeEntity, Long> {

    List<TdtProgrammeEntity> findByChannelNormalizedAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            String channelNormalized,
            ZonedDateTime now1,
            ZonedDateTime now2
        );

    void deleteAllByChannelNormalizedIn(List<String> normalizedChannels);

}
