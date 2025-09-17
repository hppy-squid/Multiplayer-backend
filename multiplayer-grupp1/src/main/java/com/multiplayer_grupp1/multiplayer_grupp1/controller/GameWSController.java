package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;
import com.multiplayer_grupp1.multiplayer_grupp1.model.StartGame;
import com.multiplayer_grupp1.multiplayer_grupp1.service.GameService;
import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerMessage;

import lombok.RequiredArgsConstructor;


// Jag tog bort @SendTo och använder istället SimpMessagingTemplate eftersom @SendTo alltid skickar till en statisk destination. 
// I vårt fall behöver vi dynamiska destinationsadresser som beror på vilket lobbyCode som används. 
// Med messagingTemplate.convertAndSend(...) kan vi bygga destinationssträngen vid körning och därmed skicka meddelanden till rätt lobby.
@Controller
@RequiredArgsConstructor
public class GameWSController {

    private final GameService gameService;
    private final LobbyService lobbyService;

    // Mapping för att skicka att användare är redo i lobbyn
    // Klient publishar till: /app/game/{lobbyCode}/ready
    // Vi broadcastar till:   /readycheck/{lobbyCode}
    @MessageMapping("/game/{lobbyCode}/ready")
    public void handleReady(@DestinationVariable String lobbyCode, Ready readyMsg){
        System.out.printf("READY msg: code=%s id=%s ready=%s%n",
                lobbyCode, readyMsg.getPlayerId(), readyMsg.isReady());
        lobbyService.setReadyAndBroadcast(lobbyCode, readyMsg.getPlayerId(), readyMsg.isReady());
    }

    // Mapping för att skicka att användare har svarat på frågan
    // Klient publishar till: /app/game/{lobbyCode}/answer
    // Vi broadcastar till: /lobby/{lobbyCode}
    @MessageMapping("/game/{lobbyCode}/answer")
    public void handleAnswer(@DestinationVariable String lobbyCode,AnswerMessage payload) {
        gameService.handleAnswer(lobbyCode, payload);
    }

    // Mapping för att låta hosta starta en match och därigenom starta för de andra också
    @MessageMapping("/game/{lobbyCode}/start")
    public void handleStart(@DestinationVariable String lobbyCode, StartGame msg) {
        System.out.println("WS START for lobby=" + lobbyCode + " playerId=" + msg.getPlayerId());
        lobbyService.startGameAndBroadcast(lobbyCode, msg.getPlayerId());
    }

    // Mapping för en publik hjälpare som skickar information till användarna och håller dem uppdaterade
    @MessageMapping("/lobby/{lobbyCode}/resync")
    public void handleResync(@DestinationVariable String lobbyCode) {
        lobbyService.broadcastSnapshotByCode(lobbyCode);
    }
}
