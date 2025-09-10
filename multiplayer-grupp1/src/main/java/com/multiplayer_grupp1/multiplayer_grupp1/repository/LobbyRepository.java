package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LobbyRepository extends JpaRepository<Lobby, Long> {

    boolean existsByLobbyCode(String lobbyCode);

    Lobby findByLobbyCode(String lobbyCode);
}
