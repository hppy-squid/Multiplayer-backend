package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om du försöker starta spel, men inte är host 
// Används ej för tillfället 
public class NotHostException extends RuntimeException {
    public NotHostException(String message) {
        super(message);
    }
}
