CREATE TABLE Paths  (
	GameId int NOT NULL,
	UserId int NOT NULL,
	PathOrder int NOT NULL,
	PageId int NOT NULL,
	PRIMARY KEY (GameId, UserId, PathOrder),
	FOREIGN KEY (GameId) REFERENCES Games(Id),
	FOREIGN KEY (UserId) REFERENCES Users(Id),
	FOREIGN KEY (PageId) REFERENCES wiki_pages(Id)
)CHARACTER SET=utf8;