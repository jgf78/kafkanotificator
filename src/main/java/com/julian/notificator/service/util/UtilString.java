package com.julian.notificator.service.util;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UtilString {
    
    public static String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_")
                   .replace("*", "\\*")
                   .replace("[", "\\[")
                   .replace("]", "\\]");
    }
}
