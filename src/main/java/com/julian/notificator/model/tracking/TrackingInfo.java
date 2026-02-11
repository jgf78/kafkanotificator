package com.julian.notificator.model.tracking;

import java.io.Serializable;

public record TrackingInfo(
        String trackNumber,
        String fromLocation,
        String toLocation,
        double weight,
        String weightUnit,
        int status,
        EventInfo lastEvent
)implements Serializable {}

