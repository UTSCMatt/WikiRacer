﻿CREATE TABLE player_game_map (
	GameId int NOT NULL,
	UserId int NOT NULL,
	CurrentPage int NOT NULL,
	StartTime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	EndTime TIMESTAMP NULL DEFAULT NULL,
	Finished BOOL NOT NULL DEFAULT FALSE,
	PRIMARY KEY (GameId, UserId),
	FOREIGN KEY (GameId) REFERENCES Games(Id),
	FOREIGN KEY (UserId) REFERENCES Users(Id),
	FOREIGN KEY (CurrentPage) REFERENCES wiki_pages(Id)
)