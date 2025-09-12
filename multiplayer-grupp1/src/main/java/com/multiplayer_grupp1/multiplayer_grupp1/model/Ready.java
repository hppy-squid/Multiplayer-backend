package com.multiplayer_grupp1.multiplayer_grupp1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ready {

    // Spelarens namn
    private String playerName; 
    
    // Lobbykoden så vi skickar information i rätt lobby 
    private String lobbyCode; 
    
    // Defaultar boolean för isReady till false och togglear när användare skickar infon 
    private boolean isReady; 

}
