# WikiRacer
## Brought to you by Team Kappa

# Table of Contents
* [Team Members](#team_members)
* [WikiRacer Description](#wikiracer_description)
* [Key Beta Version Features](#key_beta_version_features)
* [Additional Final Version Features](#additional_final_version_features)
* [Technology to Use](#technology_to_use)
* [Top 5 Technical Challenges](#top_5_technical_challenges)

# Team_Members
* Matthew Lau
* Kevin Choi
* Alden Hou

# WikiRacer_Description
WikiRacer will be an application that will allow users to play a game that uses Wikipedia. Players will start on either a randomly selected article or a user selected article and must navigate to another random or user selected article using links within each article. Additionally, certain articles may be banned from use based on user selected rules such as no dates or countries.

[Wikipedia page on the game for more info](https://en.wikipedia.org/wiki/Wikipedia:Wiki_Game)

![Flow Chart](Proposal%20Media/Game%20Flowchart.png "Flow Chart")

The web application will show the player's current statistics about the current game. This will include the current amount of time taken and the current number of clicks taken.

WikiRacer will be played synchronously or asynchronously with other players. In a synchronously game, players will join a lobby and all begin at the same time using the same starting and ending article. Players can see the number of clicks taken by each player and they will see if anyone else is finished with their finish time. Winners will be based on clicks used and/or time used defined by the lobby creator.

Asynchronous games will be given a unique code along with a leaderboard. Players can share the code allowing for anyone to attempt to be the fasted at any time after the game is created.

Additionally, the web application will have basic community such as profile pictures, leaderboards, comments on games, etc.

Ideally, the application will be like [GeoGuessr](https://en.wikipedia.org/wiki/GeoGuessr) but with Wikipedia articles instead of locations in Google Street View.

# Key_Beta_Version_Features
* Account creation and login
* Ability to create an asynchronous game
  * Ability to set up starting and ending article, or have one or both random (All players will get the same random article)
  * Ability to set up rules and ban certain articles and categories
  * Ability to set up scoring based on either clicks or time used or both (use the other as a tie breaker instead of allowing ties)
* Able to see leaderboard for asynchronous games
* Able to join an asynchronous game by inputting a code
* Backend ability to prevent access to unavailable articles or other sites

# Additional_Final_Version_Features
* Ability to create a synchronous game
  * Able to host a lobby
  * Able to join an existing lobby
  * Able to start the game
* Community features
  * User profile
  * User stats
  * Comments on games
* Ability to share results and async game codes on social media
* Popular game settings/codes shown on front page
* Play as guest/without logging in
* Ability to go backwards or back to the start with a host defined penalty

# Technology_to_Use
* [MediaWiki API](https://www.mediawiki.org/wiki/API:Main_page)
* Spring Framework
* Apache Derby (Not final, may be changed to some other DB system)
* webSockets (For synchronous games, may be switched to webRTC)

# Top_5_Technical_Challenges
1. Displaying the Wikipedia article but with unnecessary features removed such as the sidebar, header, footer, references, etc, and replacing all links to other articles with requests to the API
2. Limiting the amount of requests made to MediaWiki API. While there is no limit to read requests, it is probably better to cache certain results for about a week instead of constantly making API calls 
3. Responsive design. Trying to display both the article and game statistics on a mobile device at the same time will be difficult. May require a separate mobile site
4. Rule checking. Standard rules such as no dates or no countries may be difficult to implement as there is no category on Wikipedia called Date or Country that contains all dates and countries
5. Synchronous play
