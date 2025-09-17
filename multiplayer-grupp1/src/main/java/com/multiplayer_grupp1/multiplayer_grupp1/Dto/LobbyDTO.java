package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;

import java.util.List;

// DTO för att skicka tillbaka lobby objekt till klienten med information om lobbykod, spelarna i lobbyn och statet av game (enum)
public record LobbyDTO(Long id, String lobbyCode, List<PlayerDTO> players, GameState gameState) {
}
