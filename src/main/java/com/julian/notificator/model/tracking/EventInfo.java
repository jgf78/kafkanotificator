package com.julian.notificator.model.tracking;

import java.io.Serializable;
import java.time.LocalDateTime;

public record EventInfo(
        String action,
        LocalDateTime date,
        String service
)implements Serializable {}

