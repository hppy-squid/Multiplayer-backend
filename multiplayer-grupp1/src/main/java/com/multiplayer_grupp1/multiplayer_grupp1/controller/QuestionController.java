package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
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

    // Frontend kallar på denna för att få fråga och svarsalternativ efter frontend slumpat fram en siffra att hämta för 
    @GetMapping("/questionAndOptions")
    public List<QuestionDTO> getQuestionAndOptions(@RequestParam Long question_id) {
        System.out.println("Question id att hämta frågor och svarsalternativ för är " + question_id);
        return questionService.getQuestionById(question_id);
    }

    // Efter folk har svarat så hämtar frontenden sedan svaret på frågan från denna mappingen 
    @GetMapping("/correctAnswer")
    public AnswerDTO getCorrectAnswer(@RequestParam Long question_id) {
        System.out.println("Question id att hämta korrekt svar för är " + question_id);
        return questionService.getCorrectAnswer(question_id);
    }
  
}
