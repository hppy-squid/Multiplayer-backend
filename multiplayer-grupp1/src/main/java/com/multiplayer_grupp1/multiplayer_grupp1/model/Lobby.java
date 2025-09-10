package com.multiplayer_grupp1.multiplayer_grupp1.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;



@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lobbyCode = RandomStringUtils.randomAlphanumeric(6).toUpperCase();

    @OneToMany
    private List<Player> players = new ArrayList<>(4);

    private GameState gameState;

}
