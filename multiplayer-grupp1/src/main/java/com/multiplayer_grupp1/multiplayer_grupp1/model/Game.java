package com.multiplayer_grupp1.multiplayer_grupp1.model;

import jakarta.persistence.*;
import lombok.*;

// import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "lobby_id")
    private Lobby lobby;

    @Enumerated(EnumType.STRING)
    private GameState gameState;

    // private static final int maxQuestions = 5;

    private int currentQuestionNumber = 0;

    private Long currentQuestionId;

}
