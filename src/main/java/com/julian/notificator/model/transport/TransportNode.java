package com.julian.notificator.model.transport;

public record TransportNode(
        long id,
        double lat,
        double lon,
        NodeTags tags
    ) {}
