package com.julian.notificator.model.series;

import java.util.Arrays;

public enum StreamingPlatform {

    NETFLIX("netflix"),
    PRIME("prime"),
    HBO("hbo"),
    DISNEY("disney");

    private final String apiValue;

    StreamingPlatform(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static StreamingPlatform from(String value) {
        return Arrays.stream(values())
                .filter(p -> p.apiValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Plataforma no soportada: " + value
                        )
                );
    }
}
