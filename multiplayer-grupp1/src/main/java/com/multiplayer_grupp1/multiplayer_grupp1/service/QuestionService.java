package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.QuestionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;

    //Denna bör hämta frågan och svarsalternativen
    public QuestionDTO getQuestionById(Long question_id) {
        return questionRepository.getById(question_id);
    }

    public AnswerDTO getCorrectAnswer(Long question_id) {
        return questionRepository.getCorrectAnswerById(question_id);
    }

}
