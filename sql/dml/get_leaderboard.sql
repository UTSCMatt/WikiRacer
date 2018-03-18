DELIMITER //

CREATE PROCEDURE Get_Leaderboard (selectedGameId VARCHAR(255))
BEGIN
	DECLARE currentGameMode INT;
	SET currentGameMode = (SELECT GameMode FROM games WHERE GameId = selectedGameId);
	IF currentGameMode = 1 THEN
		SELECT users.Username, TIMESTAMPDIFF(second, StartTime, EndTime) AS TimeSpend, NumClicks FROM player_game_map INNER JOIN users ON users.Id = player_game_map.UserId WHERE GameId = (Select Id From Games WHERE GameId = selectedGameId) AND Finished = 1 ORDER BY TimeSpend ASC;
	ELSEIF currentGameMode = 2 THEN
	  SELECT users.Username, TIMESTAMPDIFF(second, StartTime, EndTime) AS TimeSpend, NumClicks FROM player_game_map INNER JOIN users ON users.Id = player_game_map.UserId WHERE GameId = (Select Id From Games WHERE GameId = selectedGameId) AND Finished = 1 ORDER BY NumClicks ASC;
	END IF;
END //