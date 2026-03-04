package com.julian.notificator.model.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NewsWebhookRequest(

        String source,
        String title,
        String link,
        String guid,

        @JsonProperty("publishedAt")
        String publishedAt,

        String author,
        String summary,

        Image image

) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(
            String url
    ) {}
}
