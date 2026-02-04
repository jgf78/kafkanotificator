package com.julian.notificator.service;

import com.julian.notificator.model.tdt.TdtProgramme;

public interface TdtCacheService {

    void cacheNow(TdtProgramme programme);

    TdtProgramme getCachedNow(String channelId);
}
