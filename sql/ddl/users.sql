﻿CREATE TABLE Users (
	Id INT NOT NULL AUTO_INCREMENT,
	Username VARCHAR(255) NOT NULL,
	Password BINARY(60) NOT NULL,
	PRIMARY KEY (Id, Username)
) CHARACTER SET=utf8;