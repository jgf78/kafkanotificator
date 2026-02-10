package com.julian.notificator.service.util.tdt;

public class UtilTdt {

    private UtilTdt() {
    }

    public static String normalizeChannel(String channel) {
        if (channel == null) return "";
        return capitalize(
                channel.replaceAll("\\.TV$", "")
                       .replaceAll("\\.es$", "")    
                       .replaceAll("\\s|\\.", "")   
                       .toLowerCase()
        );
    }
    
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
