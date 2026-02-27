package com.julian.notificator.service.impl.ipfshash;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.julian.notificator.entity.RssIpfsHash;
import com.julian.notificator.model.ipfshash.RssIpfsHashRecord;
import com.julian.notificator.repository.RssIpfsHashRepository;
import com.julian.notificator.service.RssIpfsHashService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RssIpfsHashServiceImpl implements RssIpfsHashService {

    private final RssIpfsHashRepository repository;

    @Override
    @Transactional
    public void refreshHashes(List<RssIpfsHash> hashes) {

        repository.deleteAllInBatch();

        repository.saveAll(hashes);
    }

    @Override
    public List<RssIpfsHashRecord> getAllHashes() {
        return repository.findAll().stream()
                .map(entity -> new RssIpfsHashRecord(
                        entity.getTitle(),
                        entity.getGroup(),
                        entity.getHash(),
                        entity.getGenerated()
                ))
                .toList();
    }
}
