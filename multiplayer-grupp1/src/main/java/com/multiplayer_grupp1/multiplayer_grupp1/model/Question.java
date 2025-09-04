package com.multiplayer_grupp1.multiplayer_grupp1.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Question {
    // Id för fråga 
    @Id
    @GeneratedValue
    private UUID questionId;

    private String correctAnswer;

    // Bör nog göras om till lista då det troligtvis ska bestå av tre stycken 
    private String incorrectAnswer; 

    private String difficulty;

    private String category; 

    private String question; 

    private String type; 


}
