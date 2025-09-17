package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om man försöker svara flera gånger 
// Används ej för tillfället
public class PlayerHasAlreadyAnswered extends RuntimeException{
    public PlayerHasAlreadyAnswered(String message) {
        super(message);
    }
}
