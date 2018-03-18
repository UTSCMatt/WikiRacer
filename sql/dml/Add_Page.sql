DELIMITER //

CREATE PROCEDURE Add_Page (newTitle VARCHAR(255))
BEGIN
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=newTitle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (newTitle);
	END IF;
END //
