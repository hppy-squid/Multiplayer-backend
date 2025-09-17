package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för när man försöker starta, men ej nog många spelare i lobbyn än
// Används ej för tillfället 
public class InsufficentPlayerException extends RuntimeException{
    public InsufficentPlayerException(String message) {
        super(message);
    }
}
