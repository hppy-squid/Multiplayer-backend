package com.multiplayer_grupp1.multiplayer_grupp1.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Entitet för ready
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ready {

    private Long playerId;

    // Spelarens namn
    private String playerName; 
    
    // Lobbykoden så vi skickar information i rätt lobby 
    private String lobbyCode; 
    
    // Defaultar boolean för isReady till false och togglear när användare skickar infon 
    @JsonProperty("ready")       // klienten skickar 'ready'
    private boolean ready;       

}
