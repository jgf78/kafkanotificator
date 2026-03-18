package com.julian.notificator.service.impl.sportevent;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.julian.notificator.entity.SportEvent;
import com.julian.notificator.entity.SportEventLink;
import com.julian.notificator.model.sportshash.SportEventDTO;
import com.julian.notificator.repository.SportEventRepository;
import com.julian.notificator.service.SportEventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportEventServiceImpl implements SportEventService {

    private final SportEventRepository repository;

    @Override
    @Transactional
    public void refreshHashes(List<SportEvent> hashes) {

        repository.deleteAllInBatch();

        repository.saveAll(hashes);
    }

    @Override
    public List<SportEventDTO> getAllHashes() {
        return repository.findAll().stream()
                .map(entity -> new SportEventDTO(
                        entity.getEventTime(),
                        entity.getSport(),
                        entity.getCompetition(),
                        entity.getMatchName(),
                        entity.getLinks().stream()
                                .map(SportEventLink::getStreamUrl)
                                .toList()
                ))
                .toList();
    }
    
}