package com.multiplayer_grupp1.multiplayer_grupp1;

// import static org.mockito.ArgumentMatchers.booleanThat;
// import static org.mockito.ArgumentMatchers.isNotNull;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.PlayerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Player;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.PlayerRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.repository.QuestionRepository;
import com.multiplayer_grupp1.multiplayer_grupp1.service.PlayerService;

@SpringBootTest
@Transactional
class MultiplayerGrupp1ApplicationTests {

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private PlayerRepository playerRepository;

	@Autowired 
	private PlayerService playerService;

	@Test
	void contextLoads() {
	}

	// Testar jparepository metod för att kontrollera om spelare existerar med ett visst namn
	@Test
	public void PlayerRepository_existsByPlayerName_ReturnIfPlayerExists(){

		// Arrange 
		// Här sätter vi upp det så vi hämtar för ett visst playerName
		String playerName = "Billy"; 

		// Act 
		// Här hämtar vi för det playerNamet 
		boolean playerExists = playerRepository.existsByPlayerName(playerName);

		// Assert 
		// Här assertar vi vad som ska hända när vi gör så 
		// Vi assertar att vi ska få tillbaka false för att spelare med namnet Billy finns i databasen
		// Vi assertar även att playerExists inte är null
		Assertions.assertThat(playerExists).isNotNull();
		Assertions.assertThat(playerExists).isFalse();

	}

	// Testar vår metod för att hämta svarsalternativ och fråga 
	@Test
	public void QuestionRepository_getQuestionsAndOptionsById_ReturnQuestionAndOptions(){

		// Arrange 
		// Här sätter vi upp det så att vi hämtar för ett visst id
		Long question_id = 2L; 

		// Act 
		// Här nyttjar vi våran repository och hämtar mock
		List<QuestionDTO> questionAndAnswers = questionRepository.getQuestionAndOptionsById(question_id);

		// Assert 
		// Här assertar vi först att det ska inte vara null och sedan att storleken på listan ska vara 4 objekt och slutligen 
		// att storleken på listan inte ska vara 5 objekt för att kolla så vi inte har false positive
		Assertions.assertThat(questionAndAnswers).isNotNull();
		Assertions.assertThat(questionAndAnswers.size()).isEqualTo(4);
		Assertions.assertThat(questionAndAnswers.size()).isNotEqualTo(5);
	}

	// Testar vår metod för att hämta korrekta svaret på en fråga
	@Test
	public void QuestionRepository_getCorrectAnswerById_ReturnCorrectAnswer(){

		// Arrange 
		// Här sätter vi upp det så att vi hämtar för ett visst id
		Long question_id = 1L;

		// Act 
		// Här nyttjar vi våran repository och hämtar mock
		AnswerDTO correctAnswer = questionRepository.getCorrectAnswerById(question_id);

		// Assert 
		// Här assertar vi först att det ska inte vara null och sedan att svaret ska vara "Almonds"
		Assertions.assertThat(correctAnswer).isNotNull();
		Assertions.assertThat(correctAnswer.getCorrectAnswer()).isEqualTo("Almonds");
	}

	// Testar vår service metod för att skapa spelare
	@Test
	public void PlayerService_createPlayer_ReturnCreatedPlayer(){

		// Arrange 
		// Skapar användare för att testa vår metod som ska skapa användare 
		Player player1 = new Player(); 
		player1.setHost(false);
		player1.setPlayerName("Billy");
		player1.setScore(0);

		// Act 
		// Testar metoden för att skapa spelare 
		PlayerDTO newPlayer1 = playerService.createPlayer(player1);

		// Assert 
		// Assertar att newPlayer1 inte ska vara null och att namnet ska vara "Billy" som väntat 
		Assertions.assertThat(newPlayer1).isNotNull();
		Assertions.assertThat(newPlayer1.playerName()).isEqualTo("Billy");
		Assertions.assertThat(newPlayer1.playerName()).isNotEqualTo("Bob");
	}

}
