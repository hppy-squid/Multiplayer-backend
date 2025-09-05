package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.service.QuestionService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/questionAndAnswers")
    public Question getQuestionAndAnswers(@RequestParam String LobbyCode) {
        return questionService.getQuestionAndAnswers();
    }

    @GetMapping("/correctAnswer")
    public Question getCorrectAnswer(@RequestParam String LobbyCode, @RequestParam UUID questionId) {
        return questionService.getCorrectAnswer();
    }
    
    
    
}
