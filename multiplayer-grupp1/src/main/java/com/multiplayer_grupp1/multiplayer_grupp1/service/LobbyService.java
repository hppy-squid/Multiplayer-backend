package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbyDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyIsFullException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerIsAlreadyInLobbyException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.LobbyRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Ändringar:
// - Lade till metod för att lämna en lobby. Hanterar olika scenarion som att host lämnar, lobby blir tom osv.
// - Lade till null kontrollering i convertToDTO för att undvika NullPointerException när en lobby är tom.


@Service
@RequiredArgsConstructor
public class LobbyService {

    private final PlayerService playerService;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;   // WS-broadcast till klienterna

    // Konvertera mellan entity och DTO
    public LobbyDTO convertToDTO(Lobby lobby) {
        // Skapar en lista av PlayerDTOs från listan av Players i lobbyn
        List<PlayerDTO> playerDTOS = (lobby.getPlayers() != null ? lobby.getPlayers() : List.<Player>of())
                .stream()
                .map(playerService::convertToDTO)
                .toList();

        // Returnerar en ny LobbyDTO
        return new LobbyDTO(
                lobby.getId(),
                lobby.getLobbyCode(),
                playerDTOS,
                lobby.getGameState()
                );

    }

    // Hjälpmetod: skicka snapshot till alla klienter i lobbyn
    private void broadcastLobby(Lobby lobby) {     
        messagingTemplate.convertAndSend("/lobby/" + lobby.getLobbyCode(), convertToDTO(lobby));
    }

    // Hjälpmetod när lobbyn blivit borttagen
    private void broadcastLobbyDeleted(String lobbyCode) {     
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode,
                new LobbyDTO(null, lobbyCode, List.of(), GameState.WAITING));
    }

    @Transactional
    // Skapa en ny lobby och sätter spelaren som host
    public LobbyDTO createLobby(Long playerId) {
        Lobby lobby = new Lobby();
        // Initialt sätts gameState till WAITING
        lobby.setGameState(GameState.WAITING);
        lobbyRepository.save(lobby);

        // Hämta spelaren från databasen. Om spelaren inte finns, kasta en exception
        Player player = playerRepository.findPlayerById(playerId);
        if (player == null)
            throw new PlayerNotFoundException("Player with id " + playerId + " not found");
        if (player.getLobby() != null){
            throw new PlayerIsAlreadyInLobbyException("Player is already in a lobby");
        }
        // Sätter spelaren som en host och lägger till spelaren i lobbyn
        player.setHost(true);
        player.setLobby(lobby);
        player.setReady(false);

        // Lägger till spelaren i lobbyns lista av spelare
        lobby.getPlayers().add(player);

        // Spara både spelaren och lobbyn i databasen
        playerRepository.save(player);
        lobbyRepository.save(lobby);

        // Broadcast snapshot
        broadcastLobby(lobby);

        return convertToDTO(lobby);
    }

    @Transactional
    public LobbyDTO addPlayerToLobby(String lobbyCode, Long playerId) {
        // Hittar spelaren baserat på dess ID, kastar en exception om den inte hittas
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new PlayerNotFoundException("Player not found"));
        // Hittar lobbyn baserat på dess kod, kastar en exception om den inte hittas
        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        // Kollar om lobbyn är full (max 4 spelare), kastar en exception om den är det
        if (lobby.getPlayers().size() >= 4) {
            throw new LobbyIsFullException("Lobby is full");
        }
        // Kollar om spelaren redan är i en lobby, kastar en exception om den är det
        if(player.getLobby() != null) {
            throw new PlayerIsAlreadyInLobbyException("Player is already in a lobby");
        }
        // Lägger till spelaren i lobbyn och sparar ändringarna i databasen
        lobby.getPlayers().add(player);
        player.setLobby(lobby);
        player.setReady(false);
        playerRepository.save(player);
        lobbyRepository.save(lobby);

        // Broadcast snapshot
        broadcastLobby(lobby);

        return convertToDTO(lobby);
    }

    // @Transactional gör att alla databasanrop i metoden körs som en helhet.
    // Om något går fel mitt i, rullas allt tillbaka så att databasen inte blir halvuppdaterad.
    @Transactional
    public LobbyDTO removePlayerFromLobby(String lobbyCode, Long playerId) {
        // 1) Hämta lobby + spelare
        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        // 2) Validera att spelaren verkligen tillhör just den här lobbyn
        if (player.getLobby() == null || !player.getLobby().getId().equals(lobby.getId())) {
            // Skapa gärna en egen PlayerNotInLobbyException om du vill vara tydligare
            throw new PlayerNotFoundException("Player is not in this lobby");
        }

        // 3) Ta bort relationen åt båda håll
        boolean wasHost = Boolean.TRUE.equals(player.isHost());
        lobby.getPlayers().remove(player);
        player.setLobby(null);
        player.setHost(false);
        player.setReady(false);
        playerRepository.save(player);

        // 4) Om lobbyn blev tom: radera den (eller markera som inaktiv)
        if (lobby.getPlayers().isEmpty()) {
            lobbyRepository.delete(lobby);
            // Returnera en "tom" DTO
            broadcastLobbyDeleted(lobbyCode);
            return new LobbyDTO(null, lobbyCode, List.of(), GameState.WAITING);
        }

        // 5) Om host lämnade: promota första kvarvarande spelaren till host
        if (wasHost && lobby.getPlayers().stream().noneMatch(Player::isHost)) {
            lobby.getPlayers().get(0).setHost(true);
        }

        // 6) Spara lobby och returnera uppdaterad DTO
        lobbyRepository.save(lobby);

        // Broadcast snapshot
        broadcastLobby(lobby);

        return convertToDTO(lobby);
    }

    @Transactional
    public LobbyDTO setReadyAndBroadcast(String code, Long playerId, boolean ready) {
        var lobby = lobbyRepository.findByLobbyCode(code)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
        var player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (player.getLobby() == null || !player.getLobby().getId().equals(lobby.getId()))
            throw new PlayerNotFoundException("Player is not in this lobby");

        player.setReady(ready);
        playerRepository.saveAndFlush(player);   // skriv ut direkt så det syns i DB på en gång

        messagingTemplate.convertAndSend("/lobby/" + code, convertToDTO(lobby));
        return convertToDTO(lobby);
    }
}

