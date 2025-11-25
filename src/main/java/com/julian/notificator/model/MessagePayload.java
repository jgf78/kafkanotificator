package com.julian.notificator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePayload {
    private String message;
    private String file;      
    private String filename;  
}
