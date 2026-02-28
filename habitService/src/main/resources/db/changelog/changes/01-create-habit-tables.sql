-- liquibase formatted sql
-- changeset danvo:1
CREATE TABLE habit (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id BIGINT NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       frequency VARCHAR(50) NOT NULL,
                       start_date DATE NOT NULL,
                       end_date DATE,
                       status VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE habit_tracking (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                habit_id BIGINT NOT NULL,
                                track_date DATE NOT NULL,
                                done BOOLEAN NOT NULL DEFAULT FALSE,
                                CONSTRAINT fk_habit_tracking_habit FOREIGN KEY (habit_id) REFERENCES habit(id) ON DELETE CASCADE
);