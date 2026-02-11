package com.julian.notificator.service;

import com.julian.notificator.model.tracking.TrackingInfo;

public interface TrackingService {

    TrackingInfo getTracking(String trackCode) throws Exception;

    String buildTrackingMessage(TrackingInfo trackingInfo);
    
}

