DELIMITER //

CREATE PROCEDURE Create_Game (newGameId VARCHAR(255), startTitle VARCHAR(255), endTitle VARCHAR(255))
BEGIN
	DECLARE newStartID, newEndId INT;
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=startTitle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (startTitle);
	END IF;
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=endTitle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (endTitle);
	END IF;
	SET newStartID = (SELECT Id FROM Wiki_Pages WHERE Title=startTitle);
	SET newEndId = (SELECT Id FROM Wiki_Pages WHERE Title=endTitle);
	INSERT INTO Games (GameId, StartId, EndId) Values (newGameId, newStartID, newEndId);
END //