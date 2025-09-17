package com.multiplayer_grupp1.multiplayer_grupp1.Exceptions;

// Exception för om fråga inte hittas 
// Används ej för tillfället 
public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String message) {
        super(message);
    }
}
