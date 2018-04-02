DELIMITER //

CREATE PROCEDURE User_Games (selectedUsername VARCHAR(255), showNonFinished BOOL, offsetNum INTEGER, limitNum INTEGER)
BEGIN
  IF (SELECT COUNT(Id) FROM users WHERE Username=selectedUsername) < 1 THEN
      SELECT "User Not Found";
  ELSE
	  IF (showNonFinished) THEN
		  SELECT games.GameID FROM games INNER JOIN player_game_map INNER JOIN users WHERE player_game_map.GameId = games.Id AND player_game_map.UserId = users.Id AND users.Username = selectedUsername LIMIT limitNum OFFSET offsetNum;
	  ELSE
		  SELECT games.GameID FROM games INNER JOIN player_game_map INNER JOIN users WHERE player_game_map.GameId = games.Id AND player_game_map.Finished = 1 AND player_game_map.UserId = users.Id AND users.Username = selectedUsername LIMIT limitNum OFFSET offsetNum;
	  END IF;
  END IF;
END //