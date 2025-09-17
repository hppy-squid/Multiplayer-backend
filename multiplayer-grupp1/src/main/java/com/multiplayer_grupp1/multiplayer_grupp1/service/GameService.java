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
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerMessage;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Exceptions.GameIsNotFinishedException;
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
import com.multiplayer_grupp1.multiplayer_grupp1.model.RoundState;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {


    private final GameRepository gameRepository;
    private final LobbyRepository lobbyRepository;
    private final QuestionRepository questionRepository;
    private final PlayerAnswerRepository playerAnswerRepository;
    private final PlayerRepository playerRepository;
    private final QuestionService questionService;
    private final SimpMessagingTemplate messagingTemplate;

    private final TaskScheduler taskScheduler;
    

   /*  // Används denna ens för tillfället?
    public Ready toggleReady(Ready readyMsg) {
        System.out.println("Toggleready körs iaf");
        // Bör toggla till det den inte är (detta gör den i teori återanvändbar om vi vill möjliggöra att toggla ready och inte ready)
        readyMsg.setReady(!readyMsg.isReady());
        return readyMsg;
    }

    // Används denna ens för tillfället?
    // Denna behöver också toggla tillbaka till false efter skickat signal
    public Response responded(Response response) {
        System.out.println("responded körs iaf");
        response.setHasResponded(!response.isHasResponded());
        return response;
    }


    // Används denna ens för tillfället?
    // En metod för att kontrollera om en spelare redan har svarat på en fråga
    private boolean hasPlayerAnswered(Long gameId, Long playerId, Long questionId) {
        System.out.println("hasPlayerAnswered körs iaf");
        return playerAnswerRepository.existsByGameIdAndPlayerIdAndQuestionQuestionId(gameId, playerId, questionId);
    }

    // Används denna ens för tillfället?
    // En metod för att få vilken ordning svaren har kommit in i
    private int getAnswerOrder(Long gameId, Long questionId) {
        System.out.println(" getAnswerOrder körs");
        return playerAnswerRepository.countByGameIdAndQuestionQuestionId(gameId, questionId) + 1;
    }

    // Oklart om denna ens används just nu
    // En metod för att kontrollera om alla spelare har svarat på en fråga
    private boolean allPlayersAnswered(Long gameId, Long questionId) {
        System.out.println("All players answered körs ");
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException("Game not found"));
        Lobby lobby = game.getLobby();
        int totalPlayers = lobby.getPlayers().size();
        int answeredPlayers = playerAnswerRepository.countByGameIdAndQuestionQuestionId(gameId, questionId);
        return totalPlayers == answeredPlayers;
    }

    // Oklart om denna ens körs
    // En metod för att skicka resultaten till alla spelare i spelet via WebSocket
    private void sendResults(Long gameId, Long questionId) {
        System.out.println("Sendresults körs");
        List<PlayerAnswer> results = playerAnswerRepository.findByGameIdAndQuestionQuestionId(gameId, questionId);
        messagingTemplate.convertAndSend("/response", results);
    }

    // Tror ej denna körs
    // Metod för att starta spelet
    public void startGame(String lobbyCode, Long questionId) {
        System.out.println("Metod för att starta spelet körs iaf");
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

    // Tror ej denna fungerar just nu
    // Metod för starta nästa fråga
    private void startNextQuestion(Long gameId, long questionId) {
        System.out.println("Metod för att starta nästa fråga köra iaf");
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

    // används ej för tillfället
    public void submitAnswer(Long gameId, Long playerId, String answer, Long questionId) {
        System.out.println("submitAnswer körs iaf");
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

        System.out.println("Precis innan playeranswer");
        PlayerAnswer playerAnswer = new PlayerAnswer();
        playerAnswer.setPlayer(player);
        playerAnswer.setGame(game);
        playerAnswer.setQuestion(currentQuestion);
        playerAnswer.setAnswer(answer);
        playerAnswer.setAnsweredAt(LocalDateTime.now());

        // Kontrollera om svaret är tomt eller null. Om det är det så blir svaret inkorrekt
        if (answer == null || answer.trim().isEmpty()) {
            playerAnswer.setCorrect(false);
            System.out.println("Svaret e null för" + player.getPlayerName());
        } else {
            playerAnswer.setCorrect(answer.trim().equalsIgnoreCase(currentQuestion.getCorrect_answer().trim()));
            System.out.println(playerAnswer.getAnswer() + "svaret är ej null och vi kollar om det är korrekt ");
        }

        int answerOrder = getAnswerOrder(gameId, questionId);
        playerAnswer.setAnswerOrder(answerOrder);

        playerAnswerRepository.save(playerAnswer);

        //Kontrollera att alla har svarat
        if(allPlayersAnswered(gameId, questionId)) {
            System.out.println("Metod för att beräkna poäng åkallas");
            calculateAndDistributePoints(game, currentQuestion.getQuestionId());
            gameRepository.save(game);

            if (game.getGameState() != GameState.FINISHED) {
                game.setGameState(GameState.IN_PROGRESS);
                gameRepository.save(game);
            }
            sendResults(gameId, questionId);
        }
    }

    // Åkallas inte då submitAnswer inte används för tillfället
    private void calculateAndDistributePoints(Game game, Long questionId) {
        System.out.println("calculateMetoden körs iaf");
        List<PlayerAnswer> answers = playerAnswerRepository
                .findByGameIdAndQuestionQuestionIdOrderByAnsweredAtAsc(game.getId(), questionId);

        int points = 4;

        for (PlayerAnswer answer : answers) {
            if (answer.isCorrect()) {
                answer.setPointsEarned(points);
                points = Math.max(1, points - 1);

                Player player = answer.getPlayer();
                player.setScore(player.getScore() + answer.getPointsEarned());
                System.out.println(player.getScore() + player.getPlayerName() + "Snälla visa poäng och namn här");
                playerRepository.save(player);
            } else {
                answer.setPointsEarned(0);
            }
            playerAnswerRepository.save(answer);
        }
        if (game.getCurrentQuestionNumber() >= 5) {
            long lobbyId = game.getLobby().getId();
            endGame(lobbyId);
        } else {
            game.setGameState(GameState.IN_PROGRESS);
        }

    }

    // Används inte just nu
    public void nextQuestion(Long gameId, Long questionId) {
        System.out.println("nextQuestion används iaf");
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Lobby not found"));

        if (game.getGameState() != GameState.FINISHED) {
            throw new GameIsNotFinishedException("Cannot move to next question yet");
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
        System.out.println("endGame används iaf");
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

    } */

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
            scheduleNextRoundOrFinish(lobbyCode, new Date(cur.getEndsAtEpochMillis()));
        }
    }

    private void scheduleSwitchToAnswer(String lobbyCode, int answerSeconds, Date when) {
        taskScheduler.schedule(() -> {
            var cur = rounds.get(lobbyCode);
            if (cur == null || cur.getPhase() != RoundState.Phase.QUESTION)
                return;

            cur.setPhase(RoundState.Phase.ANSWER);
            cur.setEndsAtEpochMillis(Instant.now().plusSeconds(answerSeconds).toEpochMilli());
            broadcastSnapshotByCode(lobbyCode);

            scheduleNextRoundOrFinish(lobbyCode, new Date(cur.getEndsAtEpochMillis()));
        }, when);
    }

    private void scheduleNextRoundOrFinish(String lobbyCode, Date when) {
        taskScheduler.schedule(() -> {
            var cur = rounds.get(lobbyCode);
            if (cur == null || cur.getPhase() != RoundState.Phase.ANSWER)
                return;

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

            scheduleSwitchToAnswer(lobbyCode, 5, new Date(endsAt));
        }, when);
    }

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

    private void broadcastSnapshotByCode(String lobbyCode) {
        var lobby = lobbyRepository.findByLobbyCode(lobbyCode)
                .orElseThrow(() -> new LobbyNotFoundException("Lobby not found"));
        messagingTemplate.convertAndSend("/lobby/" + lobbyCode, buildSnapshot(lobby));
    }

    public void clearRound(String lobbyCode) {
        rounds.remove(lobbyCode);
    }

}
