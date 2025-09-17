package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

// DTO för player som skickar spelare tillhörande information 
public record PlayerDTO(long id, String playerName, int score, boolean isHost, boolean ready, boolean answered) {
}
