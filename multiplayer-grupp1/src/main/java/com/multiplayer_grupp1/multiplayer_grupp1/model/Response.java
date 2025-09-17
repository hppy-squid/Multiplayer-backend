package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entitet för response, typar upp hur en repsonse ser ut
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Response {
    
    private String playerName; 

    private String lobbyCode; 

    private boolean hasResponded; 
}
