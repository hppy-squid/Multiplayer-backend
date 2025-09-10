package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.service.PlayerService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/create")
    public PlayerDTO createPlayer(@RequestBody Player player){
        return playerService.createPlayer(player);
    }
}
