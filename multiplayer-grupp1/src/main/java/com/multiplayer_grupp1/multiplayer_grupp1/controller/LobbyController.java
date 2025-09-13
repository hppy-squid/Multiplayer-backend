package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbyDTO;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.multiplayer_grupp1.multiplayer_grupp1.service.LobbyService;
import lombok.RequiredArgsConstructor;

// Ändringar:
// - Lade till SimpMessagingTemplate för att kunna skicka WebSocket-meddelanden.
// - När en lobby skapas eller en spelare ansluter så broadcastas uppdaterad lobbyinfo till alla i lobbyn.
// - Lade till @CrossOrigin("*") för att frontend (på annan domän) ska kunna anropa API:t.
// - Lade till endpoint för att lämna en lobby.


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lobby")
@CrossOrigin("*")
public class LobbyController {

    private final LobbyService lobbyService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create/{playerId}")
    public LobbyDTO createLobby(@PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.createLobby(playerId);
        messagingTemplate.convertAndSend("/lobby/" + dto.lobbyCode(), dto);
        return dto;
    }

    @PostMapping("/join/{lobbyCode}/{playerId}")
    public LobbyDTO joinLobby(@PathVariable String lobbyCode, @PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.addPlayerToLobby(lobbyCode, playerId);
        // 🔔 tala om för alla i lobbyn att players-listan ändrats
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, dto);
        return dto;
    }

    @PostMapping("/leave/{lobbyCode}/{playerId}")
        public LobbyDTO leaveLobby(@PathVariable String lobbyCode, @PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.removePlayerFromLobby(lobbyCode, playerId);
        // 🔔 tala om för alla i lobbyn att players-listan ändrats
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, dto);
        return dto;
    }

    @GetMapping("/find/{lobbyId}")
    public LobbyDTO findLobbyById(@PathVariable Long lobbyId) {
        return lobbyService.findLobbyById(lobbyId);
    }
}
