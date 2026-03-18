package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.entity.SportEvent;
import com.julian.notificator.model.sportshash.SportEventDTO;

public interface SportEventService {

    void refreshHashes(List<SportEvent> hashes);

    List<SportEventDTO> getAllHashes();

}
