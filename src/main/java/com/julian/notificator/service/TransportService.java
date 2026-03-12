package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.model.transport.TelegramStop;

public interface TransportService {

    List<TelegramStop> getStopsNearby(String latitude, String longitude, int i);

    String buildTransportMessage(List<TelegramStop> stops);

}
