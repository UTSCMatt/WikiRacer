DELIMITER //

CREATE PROCEDURE Change_Page (newGameId VARCHAR(255), player VARCHAR(255), nextPage VARCHAR(255), isFinished BOOL)
BEGIN
	DECLARE timeForEnd TIMESTAMP;
	DECLARE currentTime TIMESTAMP;
	SET currentTime = CURRENT_TIMESTAMP;
	IF isFinished THEN
		SET timeForEnd = currentTime;
	ELSE
		SET timeForEnd = NULL;
	END IF;
	UPDATE player_game_map SET NumClicks = NumClicks + 1, CurrentPage=(SELECT Id FROM Wiki_Pages WHERE Title=nextPage), EndTime=timeForEnd, Finished=isFinished WHERE GameId = (SELECT Id FROM Games WHERE GameId=newGameId) AND UserId = (SELECT Id FROM Users WHERE Username=player);
	
	SELECT NumClicks, (TIMESTAMPDIFF(SECOND,player_game_map.StartTime,currentTime)) usedTime FROM player_game_map WHERE GameId = (SELECT Id FROM Games WHERE GameId=newGameId) AND UserId = (SELECT Id FROM Users WHERE Username=player);
END //