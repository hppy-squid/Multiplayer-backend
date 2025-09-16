package com.multiplayer_grupp1.multiplayer_grupp1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.util.Iterator;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Ready;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Response;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerMessage;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.GameNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.InsufficentPlayerException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.LobbyNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerHasAlreadyAnswered;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.PlayerNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.QuestionNotFoundException;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Game;
import com.multiplayer_grupp1.multiplayer_grupp1.model.GameState;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Lobby;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.model.PlayerAnswer;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.GameRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.LobbyRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerAnswerRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.LobbySnapshotDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final TaskScheduler taskScheduler;
    

    public Ready toggleReady(Ready readyMsg) {
        // Bör toggla till det den inte är (detta gör den i teori återanvändbar om vi vill möjliggöra att toggla ready och inte ready)
        readyMsg.setReady(!readyMsg.isReady());
        return readyMsg;
    }

    // Denna behöver också toggla tillbaka till false efter skickat signal
    public Response responded(Response response) {
        response.setHasResponded(!response.isHasResponded());
        return response;
    }


    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final QuestionRepository questionRepository;
    private final PlayerAnswerRepository playerAnswerRepository;
    private final PlayerRepository playerRepository;
    private final QuestionService questionService;
    private final SimpMessagingTemplate messagingTemplate;


    // En metod för att kontrollera om en spelare redan har svarat på en fråga
    private boolean hasPlayerAnswered(Long gameId, Long playerId, Long questionId) {
        return playerAnswerRepository.existsByGameIdAndPlayerIdAndQuestionQuestionId(gameId, playerId, questionId);
    }

    // En metod för att få vilken ordning svaren har kommit in i
    private int getAnswerOrder(Long gameId, Long questionId) {
        return playerAnswerRepository.countByGameIdAndQuestionQuestionId(gameId, questionId) + 1;
    }

    // En metod för att kontrollera om alla spelare har svarat på en fråga
    private boolean allPlayersAnswered(Long gameId, Long questionId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
        Lobby lobby = game.getLobby();
        int totalPlayers = lobby.getPlayers().size();
        int answeredPlayers = playerAnswerRepository.countByGameIdAndQuestionQuestionId(gameId, questionId);
        return totalPlayers == answeredPlayers;
    }

    // En metod för att skicka resultaten till alla spelare i spelet via WebSocket
    private void sendResults(Long gameId, Long questionId) {
        List<PlayerAnswer> results = playerAnswerRepository.findByGameIdAndQuestionQuestionId(gameId, questionId);
        messagingTemplate.convertAndSend("/response", results);
    }

    // Metod för att starta spelet
    public void startGame(String lobbyCode, Long questionId) {
        // Hämta lobbyn från databasen via dess id. Kastar en LobbyNotFoundException om den inte finns
        Lobby lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));


        // Kontrollera om ett spel redan existerar för denna lobby
        Optional<Game> existingGame = gameRepository.findByLobbyId(lobby.getId());
        if (existingGame.isPresent()) {
            throw new IllegalStateException("Game already exists for this lobby");
        }

        //Error hantering om det är färre än 4 spelare i lobbyn
        if(lobby.getPlayers().size() < 4) {
            throw new InsufficentPlayerException("Not enough players to start the game");
        }

        // Skapa ett nytt spelobjekt och set deras attribut
        Game game = new Game();
        game.setLobby(lobby);
        game.setGameState(GameState.IN_PROGRESS);
        game.setCurrentQuestionNumber(1);
        game.setCurrentQuestionId(questionId);
        gameRepository.save(game);

        // Sätt lobbyns spelstatus till "IN_PROGRESS" och spara ändringen i databasen
        lobby.setGameState(GameState.IN_PROGRESS);
        lobbyRepository.save(lobby);

        // Starta den första frågan i spelet
        startNextQuestion(game.getId(), questionId);
    }

    // Metod för starta nästa fråga
    private void startNextQuestion(Long gameId, long questionId) {
        // Hämta spelet från databasen via dess id. Kastar en LobbyNotFoundException om den inte finns
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Lobby not found"));

        // Hämtar frågan från QuestionService
        List<QuestionDTO> question = questionService.getQuestionById(questionId);

        game.setCurrentQuestionId(questionId);
        gameRepository.save(game);

        // Skicka den nya frågan till alla spelare i lobbyn via WebSocket
        messagingTemplate.convertAndSend("/response", question);
    }

    public void submitAnswer(Long gameId, Long playerId, String answer, Long questionId) {
        // Hämta lobbyn från databasen via dess id. Kastar en LobbyNotFoundException om den inte finns
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));

        // Hämta spelaren från databasen via dess id. Kastar en PlayerNotFoundException om den inte finns
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        Question currentQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException("Question not found"));

        if(hasPlayerAnswered(gameId, playerId, questionId)) {
            throw new PlayerHasAlreadyAnswered("Player has already answered this question");
        }

        PlayerAnswer playerAnswer = new PlayerAnswer();
        playerAnswer.setPlayer(player);
        playerAnswer.setGame(game);
        playerAnswer.setQuestion(currentQuestion);
        playerAnswer.setAnswer(answer);
        playerAnswer.setAnsweredAt(LocalDateTime.now());
        playerAnswer.setCorrect(answer.equals(currentQuestion.getCorrect_answer()));


        // Kontrollera om svaret är tomt eller null. Om det är det så blir svaret inkorrekt
        if (answer == null || answer.trim().isEmpty()) {
            playerAnswer.setCorrect(false);
        } else {
            playerAnswer.setCorrect(answer.equals(currentQuestion.getCorrect_answer()));

        }

        int answerOrder = getAnswerOrder(gameId, questionId);
        playerAnswer.setAnswerOrder(answerOrder);

        //Kontrollera att alla har svarat
        if(allPlayersAnswered(gameId, questionId)) {
            calculateAndDistributePoints(game, currentQuestion.getQuestionId());
            gameRepository.save(game);

            game.setGameState(GameState.IN_PROGRESS);
            gameRepository.save(game);

            sendResults(gameId, questionId);
        }
        playerAnswerRepository.save(playerAnswer);

    }

    private void calculateAndDistributePoints(Game game, Long questionId) {
        List<PlayerAnswer> answers = playerAnswerRepository
                .findByGameIdAndQuestionQuestionIdOrderByAnsweredAtAsc(game.getId(), questionId);

        int points = 4;

        for (PlayerAnswer answer : answers) {
            if (answer.isCorrect()) {
                answer.setPointsEarned(points);
                points = Math.max(1, points - 1);

                Player player = answer.getPlayer();
                player.setScore(player.getScore() + answer.getPointsEarned());
                playerRepository.save(player);
            } else {
                answer.setPointsEarned(0);
            }

            if (game.getCurrentQuestionNumber() >= 5) {
                long lobbyId = game.getLobby().getId();
                endGame(lobbyId);
            } else {
                game.setGameState(GameState.IN_PROGRESS);
            }

            playerAnswerRepository.save(answer);
        }
    }

    public void nextQuestion(Long gameId, Long questionId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Lobby not found"));

        if (game.getGameState() != GameState.FINISHED) {
            throw new IllegalStateException("Cannot move to next question yet");
        }

        if (game.getCurrentQuestionNumber() >= 5) {
            long lobbyId = game.getLobby().getId();
            endGame(lobbyId);
            return;
        }
        game.setCurrentQuestionNumber(game.getCurrentQuestionNumber() + 1);
        gameRepository.save(game);

        startNextQuestion(gameId, questionId);
    }

    private void endGame(Long lobbyId) {
        Game game = gameRepository.findByLobbyId(lobbyId)
                .orElseThrow(() -> new GameNotFoundException("Game not found"));

        game.setGameState(GameState.FINISHED);
        gameRepository.save(game);

        Lobby lobby = lobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
        lobby.setGameState(GameState.FINISHED);
        lobbyRepository.save(lobby);

        List<Player> players = lobby.getPlayers();
        players.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));

        messagingTemplate.convertAndSend("/response", players);

    }

    public static final class RoundState {
        public enum Phase {
            QUESTION, ANSWER
        }

        private final long questionId;
        private final int index;
        private final int total;
        private final Phase phase;
        private final long endsAtEpochMillis;
        private final Integer answeredCount; // Integer (kan vara null) för att undvika "int != null"-fel
        // private final Map<String, RoundState> rounds = new ConcurrentHashMap<>();

        public RoundState(long questionId, int index, int total, Phase phase, long endsAtEpochMillis,
                Integer answeredCount) {
            this.questionId = questionId;
            this.index = index;
            this.total = total;
            this.phase = phase;
            this.endsAtEpochMillis = endsAtEpochMillis;
            this.answeredCount = answeredCount;
        }

        public long getQuestionId() {
            return questionId;
        }

        public int getIndex() {
            return index;
        }

        public int getTotal() {
            return total;
        }

        public Phase getPhase() {
            return phase;
        }

        public long getEndsAtEpochMillis() {
            return endsAtEpochMillis;
        }

        public Integer getAnsweredCount() {
            return answeredCount;
        }
    }

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
    long questionId = pickExistingQuestionId();
    long endsAt = Instant.now().plusSeconds(questionSeconds).toEpochMilli();

    rounds.put(lobbyCode, new RoundState(
        questionId, 0, total, RoundState.Phase.QUESTION, endsAt, 0
    ));

    // direkt snapshot (så alla ser första frågan)
    broadcastSnapshotByCode(lobbyCode);

    // schemalägg automatisk växling till ANSWER
    scheduleSwitchToAnswer(lobbyCode, answerSeconds, new Date(endsAt));
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

        throw new RuntimeException("No questions found");
    }

    public void handleAnswer(String lobbyCode, AnswerMessage msg) {
  var cur = rounds.get(lobbyCode);
  if (cur == null || cur.getPhase() != RoundState.Phase.QUESTION) return;

  // räkna upp answeredCount
  int prev = (cur.getAnsweredCount() == null ? 0 : cur.getAnsweredCount());
  var bumped = new RoundState(cur.getQuestionId(), cur.getIndex(), cur.getTotal(),
                              cur.getPhase(), cur.getEndsAtEpochMillis(), prev + 1);
  rounds.put(lobbyCode, bumped);

  // Kolla om alla är klara
  int totalPlayers = lobbyRepository.findByLobbyCode(lobbyCode)
      .map(l -> l.getPlayers() == null ? 0 : l.getPlayers().size())
      .orElse(0);

  if (totalPlayers > 0 && bumped.getAnsweredCount() >= totalPlayers) {
    // hoppa till ANSWER direkt
    var answer = new RoundState(
      cur.getQuestionId(), cur.getIndex(), cur.getTotal(),
      RoundState.Phase.ANSWER,
      Instant.now().plusSeconds(10).toEpochMilli(), // 10 sek visning (justera)
      bumped.getAnsweredCount()
    );
    rounds.put(lobbyCode, answer);
    broadcastSnapshotByCode(lobbyCode);

    // planera nästa fråga/avslut efter answer-fasen
    scheduleNextRoundOrFinish(lobbyCode, new Date(answer.getEndsAtEpochMillis()));
  } else {
    // bara uppdatera räkningen
    broadcastSnapshotByCode(lobbyCode);
  }
}

    private void scheduleSwitchToAnswer(String lobbyCode, int answerSeconds, Date when) {
  taskScheduler.schedule(() -> {
    var cur = rounds.get(lobbyCode);
    if (cur == null || cur.getPhase() != RoundState.Phase.QUESTION) return;

    var updated = new RoundState(
      cur.getQuestionId(), cur.getIndex(), cur.getTotal(),
      RoundState.Phase.ANSWER,
      Instant.now().plusSeconds(answerSeconds).toEpochMilli(),
      cur.getAnsweredCount()
    );
    rounds.put(lobbyCode, updated);
    broadcastSnapshotByCode(lobbyCode);

    // planera start av nästa fråga (eller avslut) när answer-fasen tar slut
    scheduleNextRoundOrFinish(lobbyCode, new Date(updated.getEndsAtEpochMillis()));

  }, when);
}

private void scheduleNextRoundOrFinish(String lobbyCode, Date when) {
  taskScheduler.schedule(() -> {
    var cur = rounds.get(lobbyCode);
    if (cur == null || cur.getPhase() != RoundState.Phase.ANSWER) return;

    // Sista frågan?
    if (cur.getIndex() + 1 >= cur.getTotal()) {
      // Avsluta spel
      var lobbyOpt = lobbyRepository.findByLobbyCode(lobbyCode);
      lobbyOpt.ifPresent(lobby -> {
        lobby.setGameState(GameState.FINISHED);
        lobbyRepository.save(lobby);
      });
      rounds.remove(lobbyCode);
      broadcastSnapshotByCode(lobbyCode);
      return;
    }

    // Annars starta nästa QUESTION
    long nextQ = pickExistingQuestionId();
    long endsAt = Instant.now().plusSeconds(15).toEpochMilli(); // 15 sek fråga (justera)
    var next = new RoundState(nextQ, cur.getIndex() + 1, cur.getTotal(),
                              RoundState.Phase.QUESTION, endsAt, 0);
    rounds.put(lobbyCode, next);
    broadcastSnapshotByCode(lobbyCode);

    // Planera nästa växling till ANSWER
    scheduleSwitchToAnswer(lobbyCode, 10, new Date(endsAt)); // 10 sek svar (justera)
  }, when);
}

private LobbySnapshotDTO buildSnapshot(Lobby lobby) {
    var playersWire = (lobby.getPlayers() == null ? List.<Player>of() : lobby.getPlayers())
        .stream()
        .map(p -> new LobbySnapshotDTO.PlayerWire(
            p.getId(),
            p.getPlayerName(),
            Boolean.TRUE.equals(p.isHost()),
            Boolean.TRUE.equals(p.isReady()),
            p.getScore()
        ))
        .toList();

    var r = rounds.get(lobby.getLobbyCode()); 
    LobbySnapshotDTO.RoundDTO roundDTO = null;
    if (r != null) {
        roundDTO = new LobbySnapshotDTO.RoundDTO(
            r.getQuestionId(),
            r.getIndex(),
            r.getTotal(),
            r.getPhase().name().toLowerCase(),   // "question"|"answer"
            r.getEndsAtEpochMillis(),
            r.getAnsweredCount()
        );
    }

    return new LobbySnapshotDTO(
        lobby.getLobbyCode(),
        lobby.getGameState().name(),
        playersWire,
        roundDTO
    );
}

private void broadcastSnapshotByCode(String lobbyCode) {
    var lobby = lobbyRepository.findByLobbyCode(lobbyCode)
        .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
    messagingTemplate.convertAndSend("/lobby/" + lobbyCode, buildSnapshot(lobby));
}

public void clearRound(String lobbyCode) {
    rounds.remove(lobbyCode);
}

}
