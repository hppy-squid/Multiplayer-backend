package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;
import com.multiplayer_grupp1.multiplayer_grupp1.service.GameService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GameWSController {

    private final GameService gameService;

    // Mapping för att skicka att användare är redo i lobbyn
    @MessageMapping("/game/{lobbyCode}")
    @SendTo("/readycheck") 
    public Ready handleReady(@DestinationVariable String lobbyCode, Ready readyMsg){
        gameService.toggleReady(readyMsg);
        System.out.println(readyMsg.getPlayerName());
        return readyMsg;
    }


    /* // Mapping för att skicka att användare har svarat på frågan 
    @MessageMapping("/game/{lobbyCode}")
    @SendTo("/response") */

    /* // Mapping för att uppdatera tiden 
    @MessageMapping("/game/{lobbyCode}")
    @SendTo("/timer") */
}
