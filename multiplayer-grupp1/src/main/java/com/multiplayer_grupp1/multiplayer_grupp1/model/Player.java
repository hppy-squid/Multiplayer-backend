package com.multiplayer_grupp1.multiplayer_grupp1.model;

import jakarta.persistence.*;
import lombok.*;

// Entitet för spelare, som typar upp hur de ser ut
@Entity
@RequiredArgsConstructor
@Getter
@Setter
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String playerName;

    private int score = 0;

    private boolean isHost = false;

    @Column(name = "is_ready", nullable = false)
    private boolean isReady = false;

    @ManyToOne
    @JoinColumn(name = "lobby_id")
    private Lobby lobby;
}
