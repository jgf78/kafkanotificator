package com.julian.notificator.model.crypto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CryptoRecord(
    String name,
    String symbol,
    @JsonProperty("current_price") double price,
    @JsonProperty("price_change_percentage_24h") double change24h,
    @JsonProperty("market_cap") long marketCap
) {}
