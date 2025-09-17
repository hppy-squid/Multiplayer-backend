package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om en spelare inte hittas
public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
