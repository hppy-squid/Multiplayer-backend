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

    List<Question> findByCategory(Category category);

    List<Question> findByDifficulty(Difficulty difficulty);

    List<Question> findByCategoryAndDifficulty(Category category, Difficulty difficulty);

    Optional<Question> findById(Long questionId);

    
}
