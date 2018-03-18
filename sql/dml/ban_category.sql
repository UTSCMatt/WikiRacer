DELIMITER //

CREATE PROCEDURE Ban_Category (newGameId VARCHAR(255), newCategory VARCHAR(255))
BEGIN
	DECLARE realGameId, realCategoryId INT;
	IF (SELECT COUNT(Id) FROM Wiki_Categories WHERE Category=newCategory) < 1 THEN
		INSERT INTO Wiki_Categories (Category) Values (newCategory);
	END IF;
	SET realGameId = (SELECT Id FROM Games WHERE GameId=newGameId);
	SET realCategoryId = (SELECT Id FROM Wiki_Categories WHERE Category=newCategory);
	INSERT INTO banned_categories (GameId, CategoryId) Values (realGameId, realCategoryId);
END //