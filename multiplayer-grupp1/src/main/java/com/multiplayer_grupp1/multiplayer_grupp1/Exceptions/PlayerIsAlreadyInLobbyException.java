package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om spelare redan är i lobbyn
public class PlayerIsAlreadyInLobbyException extends RuntimeException{
    public PlayerIsAlreadyInLobbyException(String message) {
        super(message);
    }
}
