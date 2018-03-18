CREATE TABLE wiki_links (
	Parent int NOT NULL,
	Child int NOT NULL,
	PRIMARY KEY (Parent, Child),
	FOREIGN KEY (Parent) REFERENCES Wiki_Pages(Id),
	FOREIGN KEY (Child) REFERENCES Wiki_Pages(Id)
) CHARACTER SET=utf8;