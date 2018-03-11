-- phpMyAdmin SQL Dump
-- version 4.0.6
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 29. Jul 2015 um 18:30
-- Server Version: 5.6.13
-- PHP-Version: 5.4.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `kf2stats`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `kf2stats`
--

CREATE TABLE IF NOT EXISTS `kf2stats` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `UID` varchar(30) NOT NULL,
  `Exp` int(11) NOT NULL,
  `Kills` int(11) NOT NULL,
  `Playtime` int(11) NOT NULL,
  `Lastperk` varchar(50) NOT NULL,
  `Playername` varchar(50) NOT NULL,
  `Avatar` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UID` (`UID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `perkstats`
--

CREATE TABLE IF NOT EXISTS `perkstats` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `UID` varchar(30) NOT NULL,
  `Perkname` varchar(50) NOT NULL,
  `Statname` varchar(50) NOT NULL,
  `Value` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `perktraits`
--

CREATE TABLE IF NOT EXISTS `perktraits` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `UID` varchar(30) NOT NULL,
  `Perkname` varchar(20) NOT NULL,
  `Traitname` varchar(30) NOT NULL,
  `Value` int(11) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
