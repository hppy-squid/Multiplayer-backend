package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import jakarta.persistence.Entity;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Player {

    private UUID id;

    private String playerName;

    private int score;

    private boolean isHost;
}
