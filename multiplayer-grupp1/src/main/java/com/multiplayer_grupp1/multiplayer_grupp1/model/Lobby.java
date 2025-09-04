package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Lobby {

    private String lobbyCode;

    private int maxPlayers;

    private List<Player> players;

    private GameState gameState;

}
