package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lobby")
public class LobbyController {

    private final LobbyService lobbyService;
}
