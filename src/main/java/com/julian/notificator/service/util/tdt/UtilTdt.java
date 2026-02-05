package com.julian.notificator.service.util.tdt;

public class UtilTdt {

    private UtilTdt() {
    }

    public static String normalizeChannel(String channel) {
        if (channel == null) return "";
        return channel.replaceAll("\\.TV$", "")
                      .replaceAll("\\s|\\.", "")
                      .toLowerCase();
    }
}
