package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository för player
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Metod för att hitta spelare baserat på deras id
    Player findPlayerById(Long id);

    // Metod för att hitta spelare baserat på deras playername
    boolean existsByPlayerName(String playerName);
}
