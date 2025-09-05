package com.multiplayer_grupp1.multiplayer_grupp1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;


    public String getQuestionAndAnswers() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQuestionAndAnswers'");
    }
    
    public String getCorrectAnswer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCorrectAnswer'");
    }


}
