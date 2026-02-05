package com.julian.notificator.model.tdt;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
public class TdtProgramme implements Serializable{
   
    private static final long serialVersionUID = 1L;
    private String channelId;
    private String channelDesc;
    private String title;
    private String desc;
    private ZonedDateTime start;
    private ZonedDateTime stop;
}

