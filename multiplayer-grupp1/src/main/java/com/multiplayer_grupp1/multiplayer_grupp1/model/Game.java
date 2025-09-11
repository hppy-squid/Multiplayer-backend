package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class Game {

    private final String lobbyCode;

    private final List<Question> questions;

    private int index = -1;

    private Question currentQuestion;

    // Håller koll på vilka frågor som tagits tidigare, måste kontrolleras mot när vi hämtar frågor 
    private List<String> previousQuestionIds; 

}
