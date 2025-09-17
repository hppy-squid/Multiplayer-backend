package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Repository för lobby, har metod för att hitta lobby baserat på lobbycode
public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    Optional<Lobby> findByLobbyCode(String lobbyCode);
}
