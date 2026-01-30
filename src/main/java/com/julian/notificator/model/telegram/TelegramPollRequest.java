package com.julian.notificator.model.telegram;

import java.util.List;

import lombok.Data;

@Data
public class TelegramPollRequest {
    private String question;              // La pregunta de la encuesta
    private List<String> options;         // Opciones (2 a 10)
    private boolean anonymous = true;     // Si la encuesta es anónima
    private boolean multipleAnswers = false; // Permitir múltiples respuestas
    private String type = "regular";      // "regular" o "quiz"
    private Integer correctOptionId;      // Solo para "quiz"
}

