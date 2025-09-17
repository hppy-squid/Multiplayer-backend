package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om du försöker spela som spelarnamn som redan finns
public class PlayerAlreadyExists extends RuntimeException {
    public PlayerAlreadyExists(String message) {
        super(message);
    }
}
