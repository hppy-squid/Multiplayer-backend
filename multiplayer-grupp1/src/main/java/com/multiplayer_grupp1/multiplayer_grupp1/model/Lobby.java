package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Lobby {

    private String lobbyCode;

    private int maxPlayers;

    private List<Player> players;

    private GameState gameState;

}
