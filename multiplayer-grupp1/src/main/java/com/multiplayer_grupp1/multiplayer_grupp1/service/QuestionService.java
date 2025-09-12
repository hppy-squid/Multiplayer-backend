package com.multiplayer_grupp1.multiplayer_grupp1.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;

    //Denna bör hämta frågan och svarsalternativen
    public List<QuestionDTO> getQuestionById(Long question_id) {
        return questionRepository.getQuestionAndOptionsById(question_id);
    }

    public AnswerDTO getCorrectAnswer(Long question_id) {
        return questionRepository.getCorrectAnswerById(question_id);
    }

}
