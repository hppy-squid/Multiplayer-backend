package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbyDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbySnapshotDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyIsFullException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerIsAlreadyInLobbyException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.model.RoundState; // <-- viktigt: modellens RoundState
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
    private final SimpMessagingTemplate messagingTemplate;

    // Behövs för att hämta/initialisera aktuell runda
    private final GameService gameService;

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

    // ---- Snapshot-bygge + broadcast ----

    /**
     * Bygger snapshot (players + gameState + round) som frontend lyssnar på via
     * /lobby/{code}.
     */
    private LobbySnapshotDTO buildSnapshot(Lobby lobby) {
        RoundState r = gameService.getRoundForLobby(lobby.getLobbyCode()); // <-- istället för rounds.get(...)

        var playersWire = (lobby.getPlayers() == null ? List.<Player>of() : lobby.getPlayers())
                .stream()
                .map(p -> new LobbySnapshotDTO.PlayerWire(
                        p.getId(),
                        p.getPlayerName(),
                        Boolean.TRUE.equals(p.isHost()),
                        Boolean.TRUE.equals(p.isReady()),
                        p.getScore(),
                        r != null && r.hasAnswered(p.getId().intValue()),
                        r != null ? r.getCorrectness(p.getId().intValue()) : null))
                .toList();

        LobbySnapshotDTO.RoundDTO roundDTO = null;
        if (r != null) {
            roundDTO = new LobbySnapshotDTO.RoundDTO(
                    r.getQuestionId(),
                    r.getIndex(),
                    r.getTotal(),
                    r.getPhase().name().toLowerCase(),
                    r.getEndsAtEpochMillis(),
                    r.getAnsweredCount());
        }

        return new LobbySnapshotDTO(
                lobby.getLobbyCode(),
                lobby.getGameState().name(), // "WAITING" | "IN_GAME" | "FINISHED"
                playersWire,
                roundDTO);
    }


    /** Publik hjälpare som GameService kan kalla. */
    public void broadcastSnapshotByCode(String lobbyCode) {
        lobbyRepository.findByLobbyCode(lobbyCode).ifPresentOrElse(
                lobby -> messagingTemplate.convertAndSend("/lobby/" + lobbyCode, buildSnapshot(lobby)),
                () -> {
                    // Skicka en neutral snapshot om lobbyn saknas (så klienter inte kraschar)
                    var empty = new LobbySnapshotDTO(
                            lobbyCode,
                            GameState.WAITING.name(),
                            List.of(),
                            null);
                    messagingTemplate.convertAndSend("/lobby/" + lobbyCode, empty);
                });
    }

    /** Om lobbyn raderas eller är tom – skicka tom snapshot (utan round). */
    private void broadcastLobbyDeleted(String lobbyCode) {
        var empty = new LobbySnapshotDTO(
                lobbyCode,
                GameState.WAITING.name(),
                List.of(),
                null);
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, empty);
    }

    // ---- Publika flöden ----

    /** Skapar en lobby och sätter given spelare som host. */
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
        if (player.getLobby() != null)
            throw new PlayerIsAlreadyInLobbyException("Player is already in a lobby");

        player.setHost(true);
        player.setLobby(lobby);
        player.setReady(false);

        // Lägger till spelaren i lobbyns lista av spelare
        lobby.getPlayers().add(player);

        // Spara både spelaren och lobbyn i databasen
        playerRepository.save(player);
        lobbyRepository.save(lobby);

        // Broadcast snapshot
        broadcastSnapshotByCode(lobby.getLobbyCode());
        
        return convertToDTO(lobby);
    }

    /** Lägger in spelare i befintlig lobby. */
    @Transactional
    public LobbyDTO addPlayerToLobby(String lobbyCode, Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        if (lobby.getPlayers().size() >= 4)
            throw new LobbyIsFullException("Lobby is full");
        if (player.getLobby() != null)
            throw new PlayerIsAlreadyInLobbyException("Player is already in a lobby");

        lobby.getPlayers().add(player);
        player.setLobby(lobby);
        player.setReady(false);
        playerRepository.save(player);
        lobbyRepository.save(lobby);

        // Broadcast snapshot
        broadcastSnapshotByCode(lobbyCode);
        
        return convertToDTO(lobby);
    }

    /** Tar bort spelare från lobby. Raderar lobby om den blir tom. */
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
        broadcastSnapshotByCode(lobbyCode);

        return convertToDTO(lobby);
    }

    /** Sätter/av-sätter ready på en spelare och broadcastar snapshot. */
    @Transactional
    public LobbyDTO setReadyAndBroadcast(String lobbyCode, Long playerId, boolean ready) {
        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (player.getLobby() == null || !player.getLobby().getId().equals(lobby.getId())) {
            throw new PlayerNotFoundException("Player is not in this lobby");
        }

        player.setReady(ready);
        playerRepository.saveAndFlush(player);

        // WS: skicka snapshot
        broadcastSnapshotByCode(lobbyCode);
        return convertToDTO(lobby);
    }

    /**
     * Nollar allas ready i lobbyn (bra för "Play again") och broadcastar snapshot.
     */
    @Transactional
    public void resetReadyAndBroadcast(String lobbyCode) {
        var lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        // 1) Nolla ready
        if (lobby.getPlayers() != null) {
            lobby.getPlayers().forEach(p -> p.setReady(false));
            playerRepository.saveAll(lobby.getPlayers());
        }

        // 2) Tillbaka till WAITING
        lobby.setGameState(GameState.WAITING);
        lobbyRepository.save(lobby);

        // 3) Nolla pågående runda (så klienten inte tror att spelet fortsätter)
        gameService.clearRound(lobbyCode);

        // 4) Broadcast ny snapshot (players + WAITING + round=null)
        broadcastSnapshotByCode(lobbyCode);
    }

    /**
     * Host startar spelet: sätt IN_GAME, initiera första rundan och broadcasta
     * snapshot (med round).
     */
    @Transactional
    public LobbyDTO startGameAndBroadcast(String code, Long playerId) {
        Lobby lobby = lobbyRepository.findByLobbyCode(code)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        if (player.getLobby() == null || !player.getLobby().getId().equals(lobby.getId()))
            throw new PlayerNotFoundException("Player is not in this lobby");
        if (!Boolean.TRUE.equals(player.isHost()))
            throw new IllegalStateException("Only host can start the game");

        lobby.setGameState(GameState.IN_GAME);
        lobbyRepository.save(lobby);

        // 1) Initiera första rundan (läggs i GameService.in-memory map)
        gameService.startFirstRound(lobby.getLobbyCode(), /* total */ 5, /* questionSec */ 15, /* answerSec */ 5);

        // 2) Bygg snapshot och skicka DIREKT
        LobbySnapshotDTO snap = buildSnapshot(lobby);
        System.out.println("DEBUG SNAPSHOT after start: " + snap); // tillfällig logg
        messagingTemplate.convertAndSend("/lobby/" + code, snap);

        return convertToDTO(lobby);
    }
}
