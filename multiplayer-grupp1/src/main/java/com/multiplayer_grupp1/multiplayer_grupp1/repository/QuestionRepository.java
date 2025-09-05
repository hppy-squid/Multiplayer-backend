package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID>{

    Question getQuestionByLobbyCode();
    
    Question getCorrectAnswer();
    
}
