package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByLobbyId(Long lobbyId);

}
