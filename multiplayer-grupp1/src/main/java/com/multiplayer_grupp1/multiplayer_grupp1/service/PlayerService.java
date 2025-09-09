package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerAlreadyExists;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;


    public Player createPlayer(Player player){

            if (playerRepository.existsByPlayerName(player.getPlayerName())) {
                throw new PlayerAlreadyExists("Player with name " + player.getPlayerName() + " already exists.");
            }
            return playerRepository.save(player);

    }

}
