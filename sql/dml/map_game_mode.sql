DELIMITER //

CREATE PROCEDURE Map_Game_Mode (newGameId VARCHAR(255), selectedGameMode VARCHAR(255))
BEGIN
	CASE
		WHEN (SELECT COUNT(Id) FROM Games WHERE GameId=newGameId) < 1 THEN
			SELECT -1, 'Game not found';
		WHEN (SELECT COUNT(GameId) FROM game_mode_map
		WHERE GameId = (SELECT Id FROM Games WHERE GameId=newGameId)) > 0 THEN
			SELECT -1, 'Game mode already set';
		WHEN (SELECT COUNT(Id) FROM game_mode WHERE GameMode=selectedGameMode) < 1 THEN
			SELECT -1, 'Game mode not found';
		ELSE
			INSERT INTO game_mode_map (GameId, GameModeId) VALUES
			((SELECT Id FROM Games WHERE GameId=newGameId),
			(SELECT Id FROM game_mode WHERE GameMode=selectedGameMode));
	END CASE;
END //