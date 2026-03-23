CREATE DATABASE IF NOT EXISTS `habitflow_db`;
USE `habitflow_db`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `databasechangelog`;
DROP TABLE IF EXISTS `databasechangeloglock`;
DROP TABLE IF EXISTS `refresh_token`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `notification_settings`;
DROP TABLE IF EXISTS `habit_tracking`;
DROP TABLE IF EXISTS `habit`;
SET FOREIGN_KEY_CHECKS = 1;