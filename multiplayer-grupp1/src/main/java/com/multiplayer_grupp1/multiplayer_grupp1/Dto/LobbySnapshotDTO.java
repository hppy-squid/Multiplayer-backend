package com.multiplayer_grupp1.multiplayer_grupp1.Dto;

import java.util.List;



import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LobbySnapshotDTO {

    private String lobbyCode;
    private String gameState; // "WAITING" | "IN_GAME" | "FINISHED" ...
    private List<PlayerWire> players;
    private RoundDTO round; // null om ingen runda igång

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerWire {
        private Long id;
        private String playerName;
        private boolean isHost;
        private boolean ready;
        private int score;
        private boolean answered;
        private Boolean correct;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoundDTO {
        private Long questionId;
        private int index; // 0-baserat
        private int total; // t.ex. 5
        private String phase;
        private long endsAt; // epoch millis
        private Integer answeredCount; // valfri
    }
}
