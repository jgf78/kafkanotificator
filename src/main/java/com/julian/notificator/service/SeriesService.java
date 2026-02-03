package com.julian.notificator.service;

import java.util.List;

import com.julian.notificator.model.series.StreamingPlatform;
import com.julian.notificator.model.series.TopSeries;

public interface SeriesService {
    
    List<TopSeries> getTopByPlatform(StreamingPlatform platform);
}

