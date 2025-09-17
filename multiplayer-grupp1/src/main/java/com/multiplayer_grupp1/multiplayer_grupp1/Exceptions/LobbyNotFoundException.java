package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för när man försöker joina en icke-existerande lobby
public class LobbyNotFoundException extends RuntimeException{
    public LobbyNotFoundException(String message) {
        super(message);
    }
}
