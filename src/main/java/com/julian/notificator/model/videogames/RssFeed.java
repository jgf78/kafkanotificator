package com.julian.notificator.model.videogames;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

public record RssFeed(String title, String description, String link, ZonedDateTime lastBuildDate, List<RssItem> items)
        implements Serializable {
}
