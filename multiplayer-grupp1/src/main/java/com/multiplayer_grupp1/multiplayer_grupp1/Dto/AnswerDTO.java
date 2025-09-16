package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

// import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswerDTO {

    private String correctAnswer; 

    private Long question_id; 

    // Skickar korrekt svar 
    public AnswerDTO(String correctAnswer, Long question_id) {
        this.correctAnswer = correctAnswer;
        this.question_id = question_id;
    }
    
}
