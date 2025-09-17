package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repository för game, har en metod för att hämta lobby baserat på lobbyid 
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByLobbyId(Long lobbyId);

}
