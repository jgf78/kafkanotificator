package com.julian.notificator.model.transport;

public record NodeTags(
        String name,
        String type,              // "bus_stop", "subway_entrance", "elevator"
        boolean wheelchair,
        String wheelchairDescription,
        boolean bus,
        boolean shelter,
        String ref,
        String website
    ) {}
