package com.multiplayer_grupp1.multiplayer_grupp1.service;

import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerAlreadyExists;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;


    private Player ConvertToEntity(PlayerDTO playerDTO) {
        Player player = new Player();
        player.setId(playerDTO.id());
        player.setPlayerName(playerDTO.playerName());
        player.setScore(playerDTO.score());
        player.setHost(playerDTO.isHost());
        return player;
    }

    private PlayerDTO ConvertToDTO(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getPlayerName(),
                player.getScore(),
                player.isHost());
    }

    public PlayerDTO createPlayer(Player player) {
        if (playerRepository.existsByPlayerName(player.getPlayerName())) {
            throw new PlayerAlreadyExists("Player with name " + player.getPlayerName() + " already exists.");
        }
        Player newPlayer = playerRepository.save(player);
        return ConvertToDTO(newPlayer);
    }
}
