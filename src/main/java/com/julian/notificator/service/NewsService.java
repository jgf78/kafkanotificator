package com.julian.notificator.service;

import java.io.IOException;

import com.rometools.rome.io.FeedException;

public interface NewsService {
    String getHeadlines() throws IllegalArgumentException, FeedException, IOException;
    
}