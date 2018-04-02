DELIMITER //

CREATE PROCEDURE Add_Path (pathArticle VARCHAR(255), inputGameId VARCHAR(255), inputUsername VARCHAR(255))
BEGIN
	DECLARE articleId, parsedGameId, playerId, nextPathId INT;
	SET articleId = (SELECT Id FROM Wiki_Pages WHERE Title=pathArticle);
	SET parsedGameId = (SELECT Id FROM Games WHERE GameId=inputGameId);
	SET playerId = (SELECT Id FROM Users WHERE Username=inputUsername);
	SET nextPathId = (SELECT COUNT(PageId) FROM Paths WHERE GameId=parsedGameId AND UserId=playerId);
	INSERT INTO Paths (GameId, UserId, PageId, PathOrder) VALUES (parsedGameId, playerId, articleId, nextPathId);
END //