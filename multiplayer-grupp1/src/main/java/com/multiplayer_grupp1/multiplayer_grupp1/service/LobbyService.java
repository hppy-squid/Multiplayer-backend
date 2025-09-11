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
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class LobbyService {

    private final PlayerService playerService;
    private final LobbyRepository lobbyRepository;
    private final PlayerRepository playerRepository;


    // Konvertera mellan entity och DTO
    public LobbyDTO convertToDTO(Lobby lobby) {
        // Skapar en lista av PlayerDTOs från listan av Players i lobbyn
        List<PlayerDTO> playerDTOS = lobby.getPlayers().stream()
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

    // Konvertera från DTO till entity
    public Lobby convertToEntity(LobbyDTO lobbyDTO) {
        Lobby lobby = new Lobby();
        lobby.setId(lobbyDTO.id());
        lobby.setLobbyCode(lobbyDTO.lobbyCode());
        lobby.setPlayers(lobbyDTO.players().stream()
                .map(playerService::convertToEntity)
                .toList());
        lobby.setGameState(lobbyDTO.gameState());
        return lobby;
    }

    public LobbyDTO findLobbyById(Long id) {
        Lobby lobby = lobbyRepository.findById(id).orElseThrow(() -> new LobbyNotFoundException("Lobby with id " + id + " not found"));
        return convertToDTO(lobby);
    }

    // Skapa en ny lobby och sätter spelaren som host
    public LobbyDTO createLobby(Long playerId) {
        Lobby lobby = new Lobby();
        // Initialt sätts gameState till WAITING
        lobby.setGameState(GameState.WAITING);
        lobbyRepository.save(lobby);

        // Hämta spelaren från databasen. Om spelaren inte finns, kasta en exception
        Player player = playerRepository.findPlayerById(playerId);
        if (player == null) {
            throw new PlayerNotFoundException("Player with id " + playerId + " not found");
        }
        // Kollar om spelaren redan är i en lobby, om så är fallet kasta en exception
        if (player.getLobby() != null) {
            throw new PlayerIsAlreadyInLobbyException("Player is already in a lobby");
        }
        // Sätter spelaren som en host och lägger till spelaren i lobbyn
        player.setHost(true);
        player.setLobby(lobby);

        // Lägger till spelaren i lobbyns lista av spelare
        lobby.getPlayers().add(player);

        // Spara både spelaren och lobbyn i databasen
        playerRepository.save(player);
        lobbyRepository.save(lobby);


        // Konvertera lobbyn till en DTO och returnerar den
        return convertToDTO(lobby);
    }

    public LobbyDTO addPlayerToLobby(String lobbyCode, Long playerId) {
        // Hittar spelaren baserat på dess ID, kastar en exception om den inte hittas
        Player player = playerRepository.findById(playerId).orElseThrow(() -> new PlayerNotFoundException("Player not found"));
        // Hittar lobbyn baserat på dess kod, kastar en exception om den inte hittas
        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode);
        if (lobby == null) {
            throw new LobbyNotFoundException("Lobby not found");
        }
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
        playerRepository.save(player);
        lobbyRepository.save(lobby);

        return convertToDTO(lobby);
    }
}
