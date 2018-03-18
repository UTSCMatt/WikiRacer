DELIMITER //

CREATE PROCEDURE Join_Game (newGameId VARCHAR(255), player VARCHAR(255))
BEGIN
	CASE
		WHEN (SELECT COUNT(Id) FROM Games WHERE GameId=newGameId) < 1 THEN
			SELECT -1, 'Game not found';
		WHEN (SELECT COUNT(GameId) FROM player_game_map
		WHERE GameId = (SELECT Id FROM Games WHERE GameId=newGameId) AND UserId = (SELECT Id FROM Users WHERE Username=player)) > 0 THEN
			SELECT -1, 'Game already joined';
		ELSE
			INSERT INTO player_game_map (GameId, UserId, CurrentPage) VALUES
			((SELECT Id FROM Games WHERE GameId=newGameId),
			(SELECT Id FROM Users WHERE Username=player),
			(SELECT StartId FROM Games WHERE GameId=newGameId));
			SELECT m.GameId, w.Title FROM player_game_map m INNER JOIN Wiki_Pages w ON m.CurrentPage=w.Id WHERE m.GameId = 
			(SELECT Id FROM Games WHERE GameId=newGameId)
			AND m.UserId =
			(SELECT Id FROM Users WHERE Username=player);
	END CASE;
END //