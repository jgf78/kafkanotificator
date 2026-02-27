package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.entity.RssIpfsHash;
import com.julian.notificator.model.ipfshash.RssIpfsHashRecord;

public interface RssIpfsHashService {
    
    void refreshHashes(List<RssIpfsHash> hashes);
   
    List<RssIpfsHashRecord> getAllHashes();
}
