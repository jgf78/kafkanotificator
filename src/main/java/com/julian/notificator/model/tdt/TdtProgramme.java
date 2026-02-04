package com.julian.notificator.model.tdt;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class TdtProgramme {
    private String channelId;
    private String channelDesc;
    private String title;
    private String desc;
    private ZonedDateTime start;
    private ZonedDateTime stop;
}

