package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
// import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.service.QuestionService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
@CrossOrigin("*")
public class QuestionController {

    private final QuestionService questionService;


    // Frontend måste slumpa siffra mellan 1-50 och skicka det till backend, 
    // frontend bör nog slumpa alla talen samtidigt och se till att de inte överensstämmer 
    // och sedan skicka ett i taget?
    @GetMapping("/questionAndOptions")
    public List<QuestionDTO> getQuestionAndOptions(@RequestParam Long question_id) {
        System.out.println("Question id att hämta frågor och svarsalternativ för är " + question_id);
        return questionService.getQuestionById(question_id);
    }

    @GetMapping("/correctAnswer")
    public AnswerDTO getCorrectAnswer(@RequestParam Long question_id) {
        System.out.println("Question id att hämta korrekt svar för är " + question_id);
        return questionService.getCorrectAnswer(question_id);
    }
  
}
