package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

// För klient → server (WS /app/game/{code}/answer)
import lombok.*;

// DTO objekt som skickar spelaren och deras svar på en fråga
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMessage {
    private Long playerId;
    private Long questionId;
    private String option; // det valda alternativet
}
