package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class Game {

    private final String lobbyCode;

    private final List<Question> questions;

    private int index = -1;

    private Question currentQuestion;

}
