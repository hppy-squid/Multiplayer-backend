package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för när spel inte hittas när man försöker joina lobby
// Används ej för tillfället
public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String message) {
        super(message);
    }
}
