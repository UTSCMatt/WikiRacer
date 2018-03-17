DELIMITER //

CREATE PROCEDURE Ban_Article (newGameId VARCHAR(255), newArticle VARCHAR(255))
BEGIN
	DECLARE realGameId, realArticleId INT;
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=newArticle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (newArticle);
	END IF;
	SET realGameId = (SELECT Id FROM Games WHERE GameId=newGameId);
	SET realArticleId = (SELECT Id FROM Wiki_Pages WHERE Title=newArticle);
	INSERT INTO banned_articles (GameId, ArticleId) Values (realGameId, realArticleId);
END //