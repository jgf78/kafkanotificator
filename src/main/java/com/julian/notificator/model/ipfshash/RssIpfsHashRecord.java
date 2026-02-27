package com.julian.notificator.model.ipfshash;

import java.time.LocalDateTime;

public record RssIpfsHashRecord(
        String title,
        String group,
        String hash,
        LocalDateTime generated
) {}
