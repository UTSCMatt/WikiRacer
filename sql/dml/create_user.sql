DELIMITER //

CREATE PROCEDURE Create_User (usernameIn VARCHAR(255), passwordIn BINARY(60))
BEGIN
	IF (SELECT COUNT(Username) FROM Users WHERE Username=usernameIn) > 0 THEN
		SIGNAL SQLSTATE 'ERROR' SET MESSAGE_TEXT = "Username already in use";
	ELSE
		INSERT INTO Users (Username, Password) VALUES (usernameIn, passwordIn);
		SELECT Username FROM Users WHERE Username=usernameIn;
	END IF;
END //