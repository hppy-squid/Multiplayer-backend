package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.*;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class Player {

    private String id = UUID.randomUUID().toString();

    private String playerName;

    private int score;

    private boolean isHost;
}
