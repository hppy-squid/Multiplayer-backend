package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Response;
import com.multiplayer_grupp1.multiplayer_grupp1.model.StartGame;
import com.multiplayer_grupp1.multiplayer_grupp1.service.GameService;
import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;

import lombok.RequiredArgsConstructor;


// Jag tog bort @SendTo och använder istället SimpMessagingTemplate eftersom @SendTo alltid skickar till en statisk destination. 
//I vårt fall behöver vi dynamiska destinationsadresser som beror på vilket lobbyCode som används. 
// Med messagingTemplate.convertAndSend(...) kan vi bygga destinationssträngen vid körning och därmed skicka meddelanden till rätt lobby.

@Controller
@RequiredArgsConstructor
public class GameWSController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
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
    // Klient publishar till: /app/game/{lobbyCode}/response
    // Vi broadcastar till:   /response/{lobbyCode}
    @MessageMapping("/game/{lobbyCode}/response")
    public void handleResponse(@DestinationVariable String lobbyCode, Response response){
        gameService.responded(response);
        System.out.println(response.getPlayerName());
        messagingTemplate.convertAndSend("/response/" + lobbyCode, response);
    }

    @MessageMapping("/game/{lobbyCode}/start") // klient publish: /app/game/{code}/start
    public void handleStart(@DestinationVariable String lobbyCode, StartGame msg) {
        lobbyService.startGameAndBroadcast(lobbyCode, msg.getPlayerId());
    }

    /*
     // Mapping för att uppdatera tiden
     * 
     * @MessageMapping("/game/{lobbyCode}")
     * 
     * @SendTo("/timer")
     */
}
