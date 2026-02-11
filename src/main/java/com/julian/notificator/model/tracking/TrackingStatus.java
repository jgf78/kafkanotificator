package com.julian.notificator.model.tracking;

public enum TrackingStatus {

    IN_PROCESS(1, "🟡 En proceso"),
    UNKNOWN(-1, "ℹ Estado desconocido");

    private final int code;
    private final String description;

    TrackingStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TrackingStatus fromCode(int code) {
        for (TrackingStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    public String getDescription() {
        return description;
    }
}

