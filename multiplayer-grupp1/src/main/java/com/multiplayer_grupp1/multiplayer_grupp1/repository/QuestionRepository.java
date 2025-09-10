package com.multiplayer_grupp1.multiplayer_grupp1.repository;


import com.multiplayer_grupp1.multiplayer_grupp1.Dto.AnswerDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.Dto.QuestionDTO;
import com.multiplayer_grupp1.multiplayer_grupp1.model.Question;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends CrudRepository<Question, Long>{

    // Hämtar fråga och svarsalternativ 
    @Query(value = """
            SELECT q.question, q.question_id, qo.option_text
            FROM question AS q
            JOIN question_options AS qo
            WHERE q.question_id = qo.question_id AND q.question_id = :question_id
            ORDER BY qo.option_text ASC;
            """, nativeQuery = true)
    List<QuestionDTO> getQuestionAndOptionsById(Long question_id);

    // Hämta korrekt svar 
    @Query(value = """
            SELECT correct_answer, question_id 
            FROM question
            WHERE question_id = :question_id
            """, nativeQuery = true)
    AnswerDTO getCorrectAnswerById(Long question_id);    
}
