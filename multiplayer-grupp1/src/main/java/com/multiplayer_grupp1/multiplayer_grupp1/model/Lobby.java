package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Lobby {

    private String lobbyCode = UUID.randomUUID().toString();

    private int maxPlayers;

    private List<Player> players = new ArrayList<>(4);

    private GameState gameState;

}
