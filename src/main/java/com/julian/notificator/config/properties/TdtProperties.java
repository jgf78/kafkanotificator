package com.julian.notificator.config.properties;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
@ConfigurationProperties(prefix = "tdt")
public class TdtProperties {

    private Set<String> nationalChannels;

}

