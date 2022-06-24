DROP DATABASE IF EXISTS DSBotScreen;
CREATE DATABASE DSBotScreen;
USE DSBotScreen;

CREATE TABLE Users (
	id						int(4)	PRIMARY KEY AUTO_INCREMENT,
	discordId				varchar(20),
	pseudo					varchar(20),
	generalLadderPosition	int(5),
	monthLadderPosition		int(5),
	totalPoints				float(10),
	monthPoints				float(10),
	numberDefencesTotal		int(5),
	numberDefencesMonth		int(5)
) ENGINE = InnoDB;

CREATE TABLE Screens (
	id			int(4)	PRIMARY KEY AUTO_INCREMENT,
	messageId	varchar(20),
	pseudo		varchar(100),
	isVictory	bool,
	versus		varchar(4),
	points		int(2),
	date 		date
) ENGINE = InnoDB;

CREATE TABLE Ladders (
	id			int(4)	PRIMARY KEY AUTO_INCREMENT,
	pseudos		varchar(3000),
	positions	varchar(600),
	date 		varchar(13)
) ENGINE = InnoDB;
	
	