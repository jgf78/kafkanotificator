package com.julian.notificator.model.videogames;

import java.io.Serializable;
import java.time.ZonedDateTime;

public record RssItem(String title, String link, String description, ZonedDateTime pubDate, String guid)
        implements Serializable {
}
