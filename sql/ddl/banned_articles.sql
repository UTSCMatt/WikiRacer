CREATE TABLE banned_articles (
	GameId int NOT NULL,
	ArticleId int NOT NULL,
	PRIMARY KEY (GameId, ArticleId),
	FOREIGN KEY (GameId) REFERENCES Games(Id),
	FOREIGN KEY (ArticleId) REFERENCES Wiki_Pages(Id)
)