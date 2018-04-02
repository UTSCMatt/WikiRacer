DELIMITER //

CREATE PROCEDURE User_Games (selectedUsername VARCHAR(255), showNonFinished BOOL, offsetNum INTEGER, limitNum INTEGER)
BEGIN
  IF (SELECT COUNT(Id) FROM Users WHERE Username=selectedUsername) < 1 THEN
      SELECT "User Not Found";
  ELSE
	  IF (showNonFinished) THEN
		  SELECT Games.GameID FROM Games INNER JOIN player_game_map INNER JOIN Users WHERE player_game_map.GameId = Games.Id AND player_game_map.UserId = Users.Id AND Users.Username = selectedUsername LIMIT limitNum OFFSET offsetNum;
	  ELSE
		  SELECT Games.GameID FROM Games INNER JOIN player_game_map INNER JOIN Users WHERE player_game_map.GameId = Games.Id AND player_game_map.Finished = 1 AND player_game_map.UserId = Users.Id AND Users.Username = selectedUsername LIMIT limitNum OFFSET offsetNum;
	  END IF;
  END IF;
END //