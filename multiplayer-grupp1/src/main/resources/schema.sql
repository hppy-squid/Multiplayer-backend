/* Måste droppa tables i denna ordningen för att det ska fungera korrekt, detta för att undvika att man skapar mer 
och mer data varje gång programmet startas, 
är ok att göra för tabeller man reseedar, men tabeller som vi lägger in data i får ej göras så med */
DROP TABLE IF EXISTS question_options; 
DROP TABLE IF EXISTS question;

/* Skapar tabell för question om den inte finns */
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

/* Skapar tabell för options om den inte finns */
CREATE TABLE IF NOT EXISTS question_options ( 
    option_ID BIGINT AUTO_INCREMENT PRIMARY KEY, 
    question_id BIGINT NOT NULL, 
    option_text VARCHAR(255) NOT NULL, 
    FOREIGN KEY (question_id) REFERENCES question(question_id)
);