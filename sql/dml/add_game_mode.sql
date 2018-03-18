DELIMITER //

CREATE PROCEDURE Add_Game_Mode (newMode VARCHAR(255))
BEGIN
	IF (SELECT COUNT(Id) FROM game_mode WHERE GameMode=newMode) < 1 THEN
		INSERT INTO game_mode (GameMode) Values (newMode);
	END IF;
END //
