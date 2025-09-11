package com.multiplayer_grupp1.multiplayer_grupp1.service;

import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;

@Service
public class GameService {

    public Ready toggleReady(Ready readyMsg) {
        // Bör toggla till det den inte är (detta gör den i teori återanvändbar om vi vill möjliggöra att toggla ready och inte ready)
        readyMsg.setReady(!readyMsg.isReady());

        return readyMsg;
    }
    
}
