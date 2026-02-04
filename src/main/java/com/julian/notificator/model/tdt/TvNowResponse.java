package com.julian.notificator.model.tdt;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class TvNowResponse {

    private String channel;
    private String title;
    private String desc;
    private ZonedDateTime start;
    private ZonedDateTime stop;
}
