package com.multiplayer_grupp1.multiplayer_grupp1.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Question {
    // Id för fråga 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    // texten i frågan
    private String text;

    // En lista med svarsalternativ
    @ElementCollection
    private List<String> options;

    // Index för det korrekta svaret i listan med svarsalternativ
    private int correctAnswerIndex;

    private Category category;

    private Difficulty difficulty;





    // Behövs specifika konstruktors för när hämtar fråga och svarsalternativ och när hämtar korrekt svar 

}
