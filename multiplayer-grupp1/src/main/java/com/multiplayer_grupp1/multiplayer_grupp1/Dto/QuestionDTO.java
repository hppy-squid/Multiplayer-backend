package com.multiplayer_grupp1.multiplayer_grupp1.Dto;


import lombok.Data;
import lombok.NoArgsConstructor;

// DTO för question som skickar med frågan, frågoid och svarsalternativs
@Data
@NoArgsConstructor
public class QuestionDTO {

    private String question; 

    private Long question_id; 

    private String option_text;


    // Konstruktor för att skicka fråga och svarsalternativ 
    public QuestionDTO(String question, Long question_id, String option_text){
        this.question = question; 
        this.question_id = question_id; 
        this.option_text = option_text;
    }

}
