package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.service.QuestionService;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private final QuestionService questionService;
    
}
