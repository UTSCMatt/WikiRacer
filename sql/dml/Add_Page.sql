DELIMITER //

CREATE PROCEDURE Add_Page (title VARCHAR(255))
BEGIN
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=title) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (title);
	END IF;
END //