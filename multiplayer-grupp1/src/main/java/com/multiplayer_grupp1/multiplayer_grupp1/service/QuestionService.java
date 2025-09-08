package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.QuestionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;

@Service
@RequiredArgsConstructor
public class QuestionService {
    
    private final QuestionRepository questionRepository;

    //Denna bör hämta frågan och svarsalternativen
    public Question getQuestionById(long id) {
        return questionRepository.findById(id).orElseThrow(() -> new QuestionNotFoundException("Question with this id not found"));
    }

    /*
    public Question getCorrectAnswer() {
        return questionRepository.getCorrectAnswer();
    }

 */


}
