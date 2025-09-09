package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Category;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>{

    // Icebox, gör MVP med enbart GENERAL_KNOWLEDGE
    List<Question> findByCategory(Category category);

    // Icebox, gör MVP med enbart EASY
    List<Question> findByDifficulty(Difficulty difficulty);

    // Hade vi inte enbart använt denna oavsett, de två ovanstående borde ej nyttjas?
    List<Question> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    // Osäker på hur denna skulle användas 
    Optional<Question> findById(Long questionId);    
}
