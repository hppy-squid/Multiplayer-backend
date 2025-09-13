package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Game;
import com.multiplayer_grupp1.multiplayer_grupp1.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/game")
@CrossOrigin("*")
public class GameController {

    private final GameService gameService;

    @PostMapping("/start/{lobbyCode}")
    public ResponseEntity<String> startGame(@PathVariable String lobbyCode , @RequestParam Long questionId) {
        gameService.startGame(lobbyCode, questionId);
        return ResponseEntity.ok("Game started");
    }

    @PostMapping("/answer/{gameId}")
    public void submitAnswer(@PathVariable Long gameId,
                             @RequestParam Long playerId,
                             @RequestParam String answer,
                             @RequestParam Long questionId) {
        gameService.submitAnswer(gameId, playerId, answer, questionId);
    }

    @PostMapping("/next/{gameId}")
    public void nextQuestion(@PathVariable Long gameId, @RequestParam Long questionId) {
        gameService.nextQuestion(gameId, questionId);
    }

}
