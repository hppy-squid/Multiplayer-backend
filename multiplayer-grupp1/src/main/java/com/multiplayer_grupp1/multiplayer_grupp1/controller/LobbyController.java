package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbyDTO;
import org.springframework.web.bind.annotation.*;

import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyService lobbyService;

    @PostMapping("/create/{playerId}")
    public LobbyDTO createLobby(@PathVariable Long playerId) {
        return lobbyService.createLobby(playerId);
    }

    @PostMapping("/join/{lobbyCode}/{playerId}")
    public LobbyDTO joinLobby(@PathVariable String lobbyCode, @PathVariable Long playerId) {
        return lobbyService.addPlayerToLobby(lobbyCode, playerId);
    }

    @GetMapping("/find/{lobbyId}")
    public LobbyDTO findLobbyById(@PathVariable Long lobbyId) {
        return lobbyService.findLobbyById(lobbyId);
    }
}
