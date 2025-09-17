package com.multiplayer_grupp1.multiplayer_grupp1.service;

import java.util.List;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.util.Iterator;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerMessage;

import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.QuestionNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.LobbyRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbySnapshotDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.RoundState;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final LobbyRepository lobbyRepository;
    private final QuestionRepository questionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final TaskScheduler taskScheduler;
    

    // Håller aktuell runda per lobby (in-memory)
    private final Map<String, RoundState> rounds = new ConcurrentHashMap<>();

    /** Används av LobbyService för att hämta rundan till snapshot. */
    public RoundState getRoundForLobby(String lobbyCode) {
        return rounds.get(lobbyCode);
    }

    /**
     * Starta första rundan (om du anropar detta från t.ex. startGameAndBroadcast).
     * Enkel implementation: slumpa en befintlig questionId och sätt QUESTION-fas i
     * `rounds`.
    */
    public void startFirstRound(String lobbyCode, int total, int questionSeconds, int answerSeconds) {
  long qId = pickExistingQuestionId();
  long endsAt = Instant.now().plusSeconds(questionSeconds).toEpochMilli();

  RoundState round = new RoundState(qId, 0, total, RoundState.Phase.QUESTION, endsAt, 0);
        round.resetAnswers(); // tydligt

  rounds.put(lobbyCode, round);
  broadcastSnapshotByCode(lobbyCode);
  scheduleSwitchToAnswer(lobbyCode, answerSeconds, new Date(endsAt), qId, 0);
}

    // Hjälpare: hitta en befintlig questionId (för enkelhet slump + fallback)
    private long pickExistingQuestionId() {
        long count = questionRepository.count();
        if (count <= 0) {
            throw new RuntimeException("No questions in DB");
        }

        // Försök hitta en giltig id slumpmässigt:
        Random rnd = new Random();
        int upper = (count > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
        upper = Math.max(1, upper);
        for (int i = 0; i < 50; i++) {
            long candidate = 1L + rnd.nextInt(upper); // [1..count] (approx)
            if (questionRepository.existsById(candidate)) {
                return candidate;
            }
        }

        // Fallback: ta första frågan via Iterable
        Iterable<Question> all = questionRepository.findAll();
        Iterator<Question> it = all.iterator();
        if (it.hasNext()) {
            return it.next().getQuestionId();
        }

        throw new QuestionNotFoundException("No questions found");
    }

    public void handleAnswer(String lobbyCode, AnswerMessage msg) {
        RoundState cur = rounds.get(lobbyCode);
        if (cur == null)
            return;

        // Tillåt bara svar i QUESTION-fasen (ändra om ni vill)
        if (cur.getPhase() != RoundState.Phase.QUESTION)
            return;

        // Hämta lobby (för scoring + antal spelare)
        var lobbyOpt = lobbyRepository.findByLobbyCode(lobbyCode);
        if (lobbyOpt.isEmpty())
            return;
        Lobby lobby = lobbyOpt.get();

        int playerId = msg.getPlayerId().intValue();

        // 1) Förhindra dubblettsvar från samma spelare
        if (cur.hasAnswered(playerId)) {
            // redan svarat – skicka snapshot ändå så klienter hålls i synk
            broadcastSnapshotByCode(lobbyCode);
            return;
        }

        // 2) Kolla korrekt svar → poängsätt (oförändrat i sak)
        AnswerDTO dto = questionRepository.getCorrectAnswerById(cur.getQuestionId());
        boolean isCorrect = dto != null
                && dto.getCorrectAnswer() != null
                && dto.getCorrectAnswer().trim().equalsIgnoreCase(
                        msg.getOption() == null ? "" : msg.getOption().trim());

        if (isCorrect) {
            var player = lobby.getPlayers().stream()
                    .filter(p -> p.getId().equals(msg.getPlayerId()))
                    .findFirst()
                    .orElse(null);
            if (player != null) {
                player.setScore(player.getScore() + 1);
                lobbyRepository.save(lobby);
            }
        }

        // 3) Markera som svarad på samma RoundState
        cur.markAnswered(playerId, isCorrect);

        // 4) Broadcast så alla ser “Answered” direkt (även i QUESTION-fasen)
        broadcastSnapshotByCode(lobbyCode);

        // 5) Om alla har svarat → växla fas till ANSWER på samma objekt
        int totalPlayers = (lobby.getPlayers() == null) ? 0 : lobby.getPlayers().size();
        Integer answeredCount = cur.getAnsweredCount();
        if (totalPlayers > 0 && answeredCount != null && answeredCount >= totalPlayers) {
            cur.setPhase(RoundState.Phase.ANSWER);
            cur.setEndsAtEpochMillis(Instant.now().plusSeconds(5).toEpochMilli()); // justera tid

            // Broadcast fasbytet
            broadcastSnapshotByCode(lobbyCode);

            // Planera nästa fråga/avslut
            scheduleNextRoundOrFinish(lobbyCode, new Date(cur.getEndsAtEpochMillis()),
                           cur.getQuestionId(), cur.getIndex());
        }
    }

    private void scheduleSwitchToAnswer(String lobbyCode, int answerSeconds, Date when,
                                    long expectedQuestionId, int expectedIndex) {
  taskScheduler.schedule(() -> {
    var cur = rounds.get(lobbyCode);
    if (cur == null) return;

    // Fortfarande samma runda?
    if (cur.getPhase() != RoundState.Phase.QUESTION) return;
    if (cur.getQuestionId() != expectedQuestionId) return;
    if (cur.getIndex() != expectedIndex) return;

    cur.setPhase(RoundState.Phase.ANSWER);
    cur.setEndsAtEpochMillis(Instant.now().plusSeconds(answerSeconds).toEpochMilli());
    broadcastSnapshotByCode(lobbyCode);

    // Nästa steg bundet till samma runda
    scheduleNextRoundOrFinish(lobbyCode, new Date(cur.getEndsAtEpochMillis()),
                              cur.getQuestionId(), cur.getIndex());
  }, when);
}

    private void scheduleNextRoundOrFinish(String lobbyCode, Date when,
                                       long expectedQuestionId, int expectedIndex) {
  taskScheduler.schedule(() -> {
    var cur = rounds.get(lobbyCode);
    if (cur == null) return;

    // Måste fortfarande vara samma runda (fas + id + index)
    if (cur.getPhase() != RoundState.Phase.ANSWER) return;
    if (cur.getQuestionId() != expectedQuestionId) return;
    if (cur.getIndex() != expectedIndex) return;

    if (cur.getIndex() + 1 >= cur.getTotal()) {
      // avsluta
      lobbyRepository.findByLobbyCode(lobbyCode).ifPresent(lobby -> {
        lobby.setGameState(GameState.FINISHED);
        lobbyRepository.save(lobby);
      });
      rounds.remove(lobbyCode);
      broadcastSnapshotByCode(lobbyCode);
      return;
    }

    // nästa fråga
    long nextQ = pickExistingQuestionId();
    long endsAt = Instant.now().plusSeconds(15).toEpochMilli(); 

    RoundState next = new RoundState(nextQ, cur.getIndex() + 1, cur.getTotal(),
        RoundState.Phase.QUESTION, endsAt, 0);
    next.resetAnswers();
    rounds.put(lobbyCode, next);
    broadcastSnapshotByCode(lobbyCode);

    // Schemaväxling till ANSWER för nästa fråga – skicka med nästa rundas id/index
    scheduleSwitchToAnswer(lobbyCode, 5, new Date(endsAt), nextQ, cur.getIndex() + 1);
  }, when);
}


    // Bygger ögonblicksbild av lobbyn och tillståndet och skickar detta med ws till klienten 
    private LobbySnapshotDTO buildSnapshot(Lobby lobby) {
        var r = rounds.get(lobby.getLobbyCode());

        var playersWire = (lobby.getPlayers() == null ? List.<Player>of() : lobby.getPlayers())
                .stream()
                .map(p -> new LobbySnapshotDTO.PlayerWire(
                        p.getId(),
                        p.getPlayerName(),
                        Boolean.TRUE.equals(p.isHost()),
                        Boolean.TRUE.equals(p.isReady()),
                        p.getScore(),
                        /* answered: */ r != null && r.hasAnswered(p.getId().intValue()),
                        /* correct: */ r != null ? r.getCorrectness(p.getId().intValue()) : null))
                .toList();

        LobbySnapshotDTO.RoundDTO roundDTO = null;
        if (r != null) {
            roundDTO = new LobbySnapshotDTO.RoundDTO(
                    r.getQuestionId(),
                    r.getIndex(),
                    r.getTotal(),
                    r.getPhase().name().toLowerCase(),
                    r.getEndsAtEpochMillis(),
                    r.getAnsweredCount());
        }

        return new LobbySnapshotDTO(
                lobby.getLobbyCode(),
                lobby.getGameState().name(),
                playersWire,
                roundDTO);
    }

    // Broadcastar snapshotten till lobbyn
    private void broadcastSnapshotByCode(String lobbyCode) {
        var lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, buildSnapshot(lobby));
    }

    public void clearRound(String lobbyCode) {
        rounds.remove(lobbyCode);
    }

}
