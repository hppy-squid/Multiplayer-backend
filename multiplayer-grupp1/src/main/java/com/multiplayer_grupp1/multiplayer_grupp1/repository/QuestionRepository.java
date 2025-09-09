package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import java.util.Optional;

import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long>{

    // Hämtar fråga och svarsalternativ 
    @Query(value = """
            SELECT * 
            FROM question 
            WHERE question_id = :question_id
            """, nativeQuery = true)
    QuestionDTO getById(Long question_id);

    // Hämta korrekt svar 
    @Query(value = """
            SELECT correct_answer, question_id 
            FROM question
            WHERE question_id = :question_id
            """, nativeQuery= true)
    AnswerDTO getCorrectAnswerById(Long question_id);    
}
