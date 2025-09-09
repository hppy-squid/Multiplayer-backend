CREATE TABLE IF NOT EXISTS question (
    question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    difficulty VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    question TEXT NOT NULL,
    correct_answer VARCHAR(255) NOT NULL,
    incorrect_answer_1 VARCHAR(255) NOT NULL,
    incorrect_answer_2 VARCHAR(255) NOT NULL,
    incorrect_answer_3 VARCHAR(255) NOT NULL
);