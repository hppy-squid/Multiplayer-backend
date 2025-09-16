package com.multiplayer_grupp1.multiplayer_grupp1.model;

public class RoundState {
    public enum Phase { QUESTION, ANSWER }

    private Long questionId;
    private int index;               // 0-baserat
    private int total;               // t.ex. 5
    private Phase phase;             // QUESTION/ANSWER
    private long endsAtEpochMillis;  // när fasen tar slut (epoch ms)
    private Integer answeredCount;   // kan vara null

    public RoundState(Long questionId, int index, int total, Phase phase, long endsAtEpochMillis, Integer answeredCount) {
        this.questionId = questionId;
        this.index = index;
        this.total = total;
        this.phase = phase;
        this.endsAtEpochMillis = endsAtEpochMillis;
        this.answeredCount = answeredCount;
    }

    public Long getQuestionId() { return questionId; }
    public int getIndex() { return index; }
    public int getTotal() { return total; }
    public Phase getPhase() { return phase; }
    public long getEndsAtEpochMillis() { return endsAtEpochMillis; }
    public Integer getAnsweredCount() { return answeredCount; }
}