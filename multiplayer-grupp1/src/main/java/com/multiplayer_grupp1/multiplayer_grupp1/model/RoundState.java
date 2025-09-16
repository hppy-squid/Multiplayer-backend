package com.multiplayer_grupp1.multiplayer_grupp1.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoundState {
    public enum Phase { QUESTION, ANSWER }

    private Long questionId;
    private int index;               // 0-baserat
    private int total;               // t.ex. 5
    private Phase phase;             // QUESTION/ANSWER
    private long endsAtEpochMillis;  // när fasen tar slut (epoch ms)
    private Integer answeredCount;   // kan vara null

    // Svar per spelare: key = playerId, value = correct? (true/false)
    private final Map<Integer, Boolean> answeredByPlayer = new HashMap<>();

    public RoundState(Long questionId, int index, int total, Phase phase, long endsAtEpochMillis, Integer answeredCount) {
        this.questionId = questionId;
        this.index = index;
        this.total = total;
        this.phase = phase;
        this.endsAtEpochMillis = endsAtEpochMillis;
        this.answeredCount = answeredCount;
    }

    /** Markera att spelaren har svarat, inkl. korrekthet. */
    public void markAnswered(int playerId, boolean correct) {
        boolean wasAbsent = !answeredByPlayer.containsKey(playerId);
        answeredByPlayer.put(playerId, correct);
        if (wasAbsent) {
            // håll answeredCount i synk med antal unika svarande spelare
            this.answeredCount = (this.answeredCount == null) ? 1 : answeredByPlayer.size();
        }
    }

    /** Har spelaren svarat? */
    public boolean hasAnswered(int playerId) {
        return answeredByPlayer.containsKey(playerId);
    }

    /** null = ej svarat, true = rätt, false = fel */
    public Boolean getCorrectness(int playerId) {
        return answeredByPlayer.get(playerId);
    }

    /** Nollställ alla svar inför ny fråga. */
    public void resetAnswers() {
        answeredByPlayer.clear();
        answeredCount = 0;
    }

    /** (valfritt) Exponera keySet om du behöver det någonstans. */
    public Set<Integer> getAnsweredPlayerIds() {
        return Collections.unmodifiableSet(answeredByPlayer.keySet());
    }

    // Mutators för fas/tidsbyte (används i schemaläggningen)
    public void setPhase(Phase phase) { this.phase = phase; }
    public void setEndsAtEpochMillis(long endsAtEpochMillis) { this.endsAtEpochMillis = endsAtEpochMillis; }

    // Getters
    public Long getQuestionId() { return questionId; }
    public int getIndex() { return index; }
    public int getTotal() { return total; }
    public Phase getPhase() { return phase; }
    public long getEndsAtEpochMillis() { return endsAtEpochMillis; }
    public Integer getAnsweredCount() { return answeredCount; }
}