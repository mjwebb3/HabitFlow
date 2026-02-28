-- liquibase formatted sql
-- changeset danvo:1
CREATE TABLE notification_settings (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       user_id BIGINT NOT NULL,
                                       channel VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
                                       address VARCHAR(255),
                                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                       status VARCHAR(50),
                                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       expiry_at DATETIME
);