package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.model.crypto.CryptoRecord;

public interface CryptoService {

    List<CryptoRecord> getTop5Cryptos();

    String buildTelegramMessage(List<CryptoRecord> cryptos);

}
