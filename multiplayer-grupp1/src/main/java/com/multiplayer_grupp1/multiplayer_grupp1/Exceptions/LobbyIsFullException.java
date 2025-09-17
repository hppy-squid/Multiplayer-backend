package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception som åkallas om du försöker joina en full lobby
public class LobbyIsFullException extends RuntimeException {
    public LobbyIsFullException(String message) {
        super(message);
    }
}
