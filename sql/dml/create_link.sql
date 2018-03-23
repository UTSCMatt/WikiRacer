DELIMITER //

CREATE PROCEDURE Create_Link (parentTitle VARCHAR(255), childTitle VARCHAR(255))
BEGIN
	DECLARE parentId, childId INT;
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=parentTitle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (parentTitle);
	END IF;
	IF (SELECT COUNT(Id) FROM Wiki_Pages WHERE Title=childTitle) < 1 THEN
		INSERT INTO Wiki_Pages (Title) Values (childTitle);
	END IF;
	SET parentID = (SELECT Id FROM Wiki_Pages WHERE Title=parentTitle);
	SET childId = (SELECT Id FROM Wiki_Pages WHERE Title=childTitle);
	IF (SELECT COUNT(Parent) FROM wiki_links WHERE Parent=parentID AND Child=childId) < 1 THEN
		INSERT INTO wiki_links (Parent, Child) Values (parentID, childId);
	END IF;
END //