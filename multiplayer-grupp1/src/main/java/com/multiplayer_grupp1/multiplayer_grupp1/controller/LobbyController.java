package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;
}
