package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionDTO {

    private Long question_id; 

    private String question; 

    private List<String> options;

    // Konstruktor för att skicka fråga och svarsalternativ 
    public QuestionDTO(Long question_id, String question, List<String> options){
        this.question_id = question_id; 
        this.question = question; 
        this.options = options;
    }

}
