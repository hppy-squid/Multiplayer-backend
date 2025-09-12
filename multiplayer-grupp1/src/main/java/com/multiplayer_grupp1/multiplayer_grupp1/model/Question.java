package com.multiplayer_grupp1.multiplayer_grupp1.model;


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

    private String type;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    private Category category;
    
    // Frågan 
    private String question;

    private String correct_answer;

    private String incorrect_answer_1;

    private String incorrect_answer_2;

    private String incorrect_answer_3;

}
