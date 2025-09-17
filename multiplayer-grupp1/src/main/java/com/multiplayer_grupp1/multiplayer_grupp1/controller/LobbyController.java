package com.multiplayer_grupp1.multiplayer_grupp1.controller;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbyDTO;

import org.springframework.http.ResponseEntity;
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

    // Mapping för att skapa lobby, då autogenreras en LobbyCode som andra användare sedan kan joina din lobby med
    @PostMapping("/create/{playerId}")
    public LobbyDTO createLobby(@PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.createLobby(playerId);
        messagingTemplate.convertAndSend("/lobby/" + dto.lobbyCode(), dto);
        return dto;
    }

    // Mapping för att joina någons lobby om du har den giltiga lobbycoden och lobbyn ej är full 
    @PostMapping("/join/{lobbyCode}/{playerId}")
    public LobbyDTO joinLobby(@PathVariable String lobbyCode, @PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.addPlayerToLobby(lobbyCode, playerId);
        // 🔔 tala om för alla i lobbyn att players-listan ändrats
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, dto);
        return dto;
    }

    // Mapping för att lämna en lobby
    @PostMapping("/leave/{lobbyCode}/{playerId}")
        public LobbyDTO leaveLobby(@PathVariable String lobbyCode, @PathVariable Long playerId) {
        LobbyDTO dto = lobbyService.removePlayerFromLobby(lobbyCode, playerId);
        // 🔔 tala om för alla i lobbyn att players-listan ändrats
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, dto);
        return dto;
    }

    // Mapping för att nollställa användares readysetting så att de då på nytt kan indikera att de är redo för att starta ett spel
    @PostMapping("/{lobbyCode}/ready/reset")
    public ResponseEntity<Void> resetReady(@PathVariable String lobbyCode) {
        lobbyService.resetReadyAndBroadcast(lobbyCode);
        return ResponseEntity.ok().build();
    }
}
