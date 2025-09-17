/* package com.multiplayer_grupp1.multiplayer_grupp1.repository;

import com.multiplayer_grupp1.multiplayer_grupp1.model.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {
    List<PlayerAnswer> findByGameIdAndQuestionQuestionIdOrderByAnsweredAtAsc(Long gameId, Long questionId);

    boolean existsByGameIdAndPlayerIdAndQuestionQuestionId(Long gameId, Long playerId, Long questionId);

    int countByGameIdAndQuestionQuestionId(Long gameId, Long questionId);

    List<PlayerAnswer> findByGameIdAndQuestionQuestionId(Long gameId, Long questionId);
}
 */