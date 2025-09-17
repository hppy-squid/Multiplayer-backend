package com.multiplayer_grupp1.multiplayer_grupp1.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

// Service för vår question
@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;

    // Hämtar en listan av QuestionDTO objekt och skickar tillbaka till klienten, då skickas frågan och svarsalternativen
    public List<QuestionDTO> getQuestionById(Long question_id) {
        return questionRepository.getQuestionAndOptionsById(question_id);
    }

    // Hämtar ett AnswerDTO objekt som består av korrekta svaret och question_id
    public AnswerDTO getCorrectAnswer(Long question_id) {
        return questionRepository.getCorrectAnswerById(question_id);
    }

}
