package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
}
