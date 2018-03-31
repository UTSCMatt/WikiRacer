(function () {
    "use strict";
    
    window.addEventListener("load", function() {
        var username = api.getUser();
        var showNonFinished = false;
        var offset = 0;
        var limit = 10;
        var profileName = document.getElementById("profile_uname");
        if (!username) {
            profileName.innerHTML = "You are not logged in";
        } else {
            username = api.getUser();
            profileName.innerHTML = "Your username is: " + username;
            api.userGames(username, showNonFinished, offset, limit, function(err, games){
              if (err) {console.log(err);
              } else {
                  getPage(games);
              }
            });

        document.getElementById("prev_page_btn").addEventListener('click', function(e) {
            if (offset - limit >= 0) {
                offset = offset - limit;
                api.userGames(username, showNonFinished, offset, limit, function(err, games){
                    if (err) console.log(err);
                    else {
                        getPage(games);
                    }
                });
            } else {
                alert("You are on the first page");
            }
        });

        document.getElementById("next_page_btn").addEventListener('click', function(e) {
            offset = offset + limit;
            api.userGames(username, showNonFinished, offset, limit, function(err, games){
                if (err) console.log(err);
                else if (games.length === 0){
                    offset = offset - limit;
                    alert("No Results");
                } else {
                    getPage(games);
                }
            });
        });

        // renders list of game codes
        function getPage (games) {
            var gameList = document.querySelector('.game_list');
            gameList.innerHTML = "";
            // adds behaviour to game codes to show stats table when clicked
            function addLinks(listElmt) {
                listElmt.addEventListener('click', function (e) {
                    api.userGamePath(listElmt.innerHTML, username, function (err, gamePath) {
                        if (err) console.log(err);
                        else {
                            api.getGameStats(listElmt.innerHTML, function(err, gameStats){
                               renderStats(gamePath, gameStats);
                            });

                        }
                    });
                });
            }
            // appends each game code to the list
            for (var index = 0; index < games.length; index++) {
                var listElmt = document.createElement("li");
                listElmt.className = "list_element";
                listElmt.innerHTML = games[index];
                addLinks(listElmt);
                gameList.appendChild(listElmt);
            }

        }

        function renderStats(gamePath, gameStats) {
            var statsForm = document.getElementById("stats_form");
            statsForm.innerHTML = "";
            // makes a table with game and user stats
            var statsTable = document.createElement("table");
            statsTable.id = "stats_table";
            statsTable.className = "table";
            statsTable.innerHTML = `<tr>
                                <th>Time Taken</th>
                                <th>Clicks Taken</th>
                                <th>Path Taken</th>
                                </tr>`;
            // inserts clicks, time, path for the user
            var path = gamePath[0];
            for (var i = 1; i < gamePath.length; i++) {
                path += " -> " + gamePath[i];
            }

            for (var i = 0; i < gameStats.length; i++){
                if(gameStats[i].username == username){
                    var time = new Date(null);
                    time.setSeconds(gameStats[i].timeSpend);
                    var finalTime = time.toISOString().substr(11, 8);
                    var numClicks = gameStats[i].numClicks;
                }
            }

            var row = statsTable.insertRow(-1);
            var cell0 = row.insertCell(0);
            var cell1 = row.insertCell(1);
            var cell2 = row.insertCell(2);
            cell0.innerHTML = finalTime;
            cell1.innerHTML = numClicks;
            cell2.innerHTML = path;

            statsForm.appendChild(statsTable);

            statsForm.className = "form";
        }

        }
        
    });
})();