package com.multiplayer_grupp1.multiplayer_grupp1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;

    //Denna bör hämta frågan och svarsalternativen
    public Question getQuestionAndAnswers() {
        return questionRepository.getQuestionByLobbyCode();
    }

    public Question getCorrectAnswer() {
        return questionRepository.getCorrectAnswer();
    }


}
