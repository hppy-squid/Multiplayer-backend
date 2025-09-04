package com.multiplayer_grupp1.multiplayer_grupp1.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Player {

    private UUID id;

    private String playerName;

    private int score;

    private boolean isHost;
}
