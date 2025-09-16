package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.*;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    // Konverterar PlayerDTO till Player entity för att spara i databasen
    public Player convertToEntity(PlayerDTO playerDTO) {
        Player player = new Player();
        player.setId(playerDTO.id());
        player.setPlayerName(playerDTO.playerName());
        player.setScore(playerDTO.score());
        player.setHost(playerDTO.isHost());
        return player;
    }

    // Konverterar Player entity till PlayerDTO för att skicka till klienten
    public PlayerDTO convertToDTO(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getPlayerName(),
                player.getScore(),
                player.isHost(),
                player.isReady(),
                false // answered: fylls i snapshot (LobbyService) utifrån RoundState
        );
    }

    // Skapar en ny spelare
    public PlayerDTO createPlayer(Player player) {
        // Kollar om spelaren redan finns i databasen baserat på spelarens namn
        if (playerRepository.existsByPlayerName(player.getPlayerName())) {
            // Om true, kastar en exception
            throw new PlayerAlreadyExists("Player with name " + player.getPlayerName() + " already exists.");
        }
        // Skapar en ny spelare och sparar den i databasen
        Player newPlayer = playerRepository.save(player);

        // Returnerar den sparade spelaren som en PlayerDTO
        return convertToDTO(newPlayer);
    }

}
