package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Response;
import com.multiplayer_grupp1.multiplayer_grupp1.service.GameService;

import lombok.RequiredArgsConstructor;


// Jag tog bort @SendTo och använder istället SimpMessagingTemplate eftersom @SendTo alltid skickar till en statisk destination. 
//I vårt fall behöver vi dynamiska destinationsadresser som beror på vilket lobbyCode som används. 
// Med messagingTemplate.convertAndSend(...) kan vi bygga destinationssträngen vid körning och därmed skicka meddelanden till rätt lobby.

@Controller
@RequiredArgsConstructor
public class GameWSController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    // Mapping för att skicka att användare är redo i lobbyn
    // Klient publishar till: /app/game/{lobbyCode}/ready
    // Vi broadcastar till:   /readycheck/{lobbyCode}
    @MessageMapping("/game/{lobbyCode}/ready")
    public void handleReady(@DestinationVariable String lobbyCode, Ready readyMsg){
        gameService.toggleReady(readyMsg);
        System.out.println(readyMsg.getPlayerName());
        messagingTemplate.convertAndSend("/readycheck/" + lobbyCode, readyMsg);
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

    /* // Mapping för att uppdatera tiden 
    @MessageMapping("/game/{lobbyCode}")
    @SendTo("/timer") */
}
