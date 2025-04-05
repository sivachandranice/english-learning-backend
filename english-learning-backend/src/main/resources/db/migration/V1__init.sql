DROP TABLE IF EXISTS sentence;
DROP TABLE IF EXISTS meaning;
DROP TABLE IF EXISTS word;

CREATE TABLE word (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      word VARCHAR(100) NOT NULL UNIQUE,
                      is_phrasal_verb BOOL NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE meaning (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         word_id BIGINT NOT NULL,
                         meaning VARCHAR(255) NOT NULL UNIQUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
);

CREATE TABLE sentence (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          word_id BIGINT NOT NULL,
                          sentence TEXT NOT NULL,
                          translated_sentence TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (word_id) REFERENCES word(id) ON DELETE CASCADE
);
