CREATE TABLE game_mode_map (
	GameId int NOT NULL,
	ModeId int NOT NULL,
	PRIMARY KEY (GameId, ModeId),
	FOREIGN KEY (GameId) REFERENCES Games(Id),
	FOREIGN KEY (ModeId) REFERENCES game_mode(Id)
)