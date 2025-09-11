package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;

import java.util.List;

public record LobbyDTO(Long id, String lobbyCode, List<PlayerDTO> players, GameState gameState) {
}
