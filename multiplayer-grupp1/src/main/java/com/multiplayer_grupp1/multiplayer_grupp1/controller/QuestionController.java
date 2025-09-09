package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.service.QuestionService;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;

    /*
        @GetMapping("/questionAndAnswers")
    public Question getQuestionAndAnswers(@RequestParam int id) {
        return questionService.getQuestionAndAnswers(id);
    }

    @GetMapping("/correctAnswer")
    public Question getCorrectAnswer(@RequestParam String lobbyCode, @RequestParam UUID questionId) {
        return questionService.getCorrectAnswer();
    }
     */

    
    
    
}
