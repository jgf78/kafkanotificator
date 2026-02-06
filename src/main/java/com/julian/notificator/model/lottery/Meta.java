package com.julian.notificator.model.lottery;

public record Meta(
        int page,
        int limit,
        int total,
        int totalPages,
        boolean hasNext,
        boolean hasPrev
) {}

