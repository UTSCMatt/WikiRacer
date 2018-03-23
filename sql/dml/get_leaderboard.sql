DELIMITER //

CREATE PROCEDURE Get_Leaderboard (selectedGameId VARCHAR(255))
BEGIN
  DECLARE currentGameMode INT;
  IF (SELECT COUNT(Id) FROM Games WHERE GameId=selectedGameId) < 1 THEN
      SELECT "Game Not Found", -1, -1;
  ELSE
      SET currentGameMode = (SELECT GameMode FROM Games WHERE GameId = selectedGameId);
      IF currentGameMode = 1 THEN
        SELECT Users.Username, TIMESTAMPDIFF(second, StartTime, EndTime) AS TimeSpend, NumClicks FROM player_game_map INNER JOIN Users ON Users.Id = player_game_map.UserId WHERE GameId = (Select Id From Games WHERE GameId = selectedGameId) AND Finished = 1 ORDER BY TimeSpend ASC;
      ELSEIF currentGameMode = 2 THEN
        SELECT Users.Username, TIMESTAMPDIFF(second, StartTime, EndTime) AS TimeSpend, NumClicks FROM player_game_map INNER JOIN Users ON Users.Id = player_game_map.UserId WHERE GameId = (Select Id From Games WHERE GameId = selectedGameId) AND Finished = 1 ORDER BY NumClicks ASC;
      END IF;
	END IF;
END //