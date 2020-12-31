# ConvictChatFilter 

## Overview

Java Plugin Made for Spigot API and uses EventHandlers to check for specific chat event from player.

## Features

* Makes use of Spigot API, Sets, Regex, EventListeners, and Factions API.
* Contains YAML configuration file which can force update or updates based on player join event.
* Contains custom YAML config file which lists the words to check for within in-game chat to restrict or replace
* Has two events, onPlayerJoin and onChatEvent. When a player joins it will check the config file for instance of player, if false it will add it using the players unique id. On a chat event it will check the string for instances of restricted words within config file and swap them depending on if the player has the plugin active.

