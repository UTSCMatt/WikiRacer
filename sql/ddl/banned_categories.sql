CREATE TABLE banned_categories (
	GameId int NOT NULL,
	CategoryId int NOT NULL,
	PRIMARY KEY (GameId, CategoryId),
	FOREIGN KEY (GameId) REFERENCES Games(Id),
	FOREIGN KEY (CategoryId) REFERENCES Wiki_Categories(Id)
)