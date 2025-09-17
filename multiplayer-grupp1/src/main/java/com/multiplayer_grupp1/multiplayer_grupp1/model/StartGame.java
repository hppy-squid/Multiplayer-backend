package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Typar upp requestsen för när hosten startar spelet
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class StartGame {
    private Long playerId; // hostens id
}
