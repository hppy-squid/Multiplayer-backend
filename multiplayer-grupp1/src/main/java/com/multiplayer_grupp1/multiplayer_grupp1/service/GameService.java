package com.multiplayer_grupp1.multiplayer_grupp1.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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



}
