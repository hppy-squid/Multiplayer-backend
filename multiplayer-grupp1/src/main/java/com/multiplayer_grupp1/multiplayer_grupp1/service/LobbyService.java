package com.multiplayer_grupp1.multiplayer_grupp1.service;

import com.multiplayer_grupp1.multiplayer_grupp1.repository.LobbyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class LobbyService {

    private final PlayerService playerService;
    private final LobbyRepository lobbyRepository;




}
