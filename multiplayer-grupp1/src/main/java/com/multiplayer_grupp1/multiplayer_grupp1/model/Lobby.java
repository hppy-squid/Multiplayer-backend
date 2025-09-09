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

    // Blir alltför långt om meningen är att detta är koden spelarna joinar med, bör nog ha 6-teckn lobbykod
    private String lobbyCode = UUID.randomUUID().toString();

    private List<Player> players = new ArrayList<>(4);

    private GameState gameState;

}
