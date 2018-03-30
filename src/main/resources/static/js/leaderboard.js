(function () {
    "use strict";
    window.addEventListener('load', function() {
        
        var offset = 0;
        var limit = 10;
        var query = window.location.hash.substring(1);



        api.getGameList(offset, limit, query, function(err, games) {
            if (err) console.log(err);
            else {
                getPage(games);
            }
        });

        // searches for games that match or are similar to the entered code 
        document.getElementById("search_btn").addEventListener('click', function(e) {
            offset = 0;
            query = document.getElementById("code_search").value;
            api.getGameList(offset, limit, query, function(err, games) {
                if (err) console.log(err);
                else {
                    getPage(games);
                }
            });
        });

        document.getElementById("prev_page_btn").addEventListener('click', function(e) {
            if (offset - limit >= 0) {
                offset = offset - limit;
                api.getGameList(offset, limit, query, function(err, games){
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
            api.getGameList(offset, limit, query, function(err, games) {
                if (err) console.log(err);
                else if (games.length === 0){
                    offset = offset - limit;
                    alert("No Results");
                } else {
                    getPage(games);
                }
            });
        });

        // adds behaviour to game codes to show stats table when clicked
        function addLinks(listElmt) {
            listElmt.addEventListener('click', function (e) {
                api.getGameStats(listElmt.innerHTML, function (err, gameStats) {
                    if (err) console.log(err);
                    else {
                        renderStats(gameStats);
                    }
                });
            });
        }

        // renders list of game codes
        function getPage (games) {
            var gameList = document.querySelector('.game_list');
            gameList.innerHTML = "";
            generateConfigTable(games, gameList, true);
        }

        // generate a table to display the game code, start/end page, and game mode
        function generateConfigTable(data, placement, link){
            var configTable = document.createElement("table");
            configTable.className = "table";
            configTable.innerHTML = `<tr>
                                <th>Game Code</th>
                                <th>Start Page</th>
                                <th>End Page</th>
                                <th>Game Mode</th>
                                </tr>`;
            var dataLength = 1;
            if(link){
              dataLength = data.length
            }
            for (var index = 0; index < dataLength; index++) {
              var row = configTable.insertRow(-1);
              var cell0 = row.insertCell(0);
              var cell1 = row.insertCell(1);
              var cell2 = row.insertCell(2);
              var cell3 = row.insertCell(3);
              if(link){
                cell0.innerHTML = data[index][0];
                cell1.innerHTML = data[index][1];
                cell2.innerHTML = data[index][2];
                cell3.innerHTML = data[index][3];
                addLinks(cell0);
                cell0.className = "list_element";
              }
              else{
                cell0.innerHTML = data.gameCode;
                cell1.innerHTML = data.startPage;
                cell2.innerHTML = data.endPage;
                cell3.innerHTML = data.gameMode;
              }
            }
            placement.appendChild(configTable);
        }

        function renderStats(gameStats) {
  
            var configForm = document.getElementById("config_form");
            configForm.innerHTML = "";

            generateConfigTable(gameStats[0], configForm, false);
            configForm.className = "form";

            var statsForm = document.getElementById("stats_form");
            statsForm.innerHTML = "";
            // makes a table with game and user stats
            var statsTable = document.createElement("table");
            statsTable.id = "stats_table";
            statsTable.className = "table";
            statsTable.innerHTML = `<tr>
                                <th>Username</th>
                                <th>Time Taken</th>
                                <th>Clicks Taken</th>
                                </tr>`;
            // inserts username, clicks, and time for each user who completed the game
            for (var i = 1; i < gameStats.length; i++) {
                var time = new Date(null);
                time.setSeconds(gameStats[i].timeSpend);
                var finalTime = time.toISOString().substr(11, 8);
                var row = statsTable.insertRow(-1);
                var cell0 = row.insertCell(0);
                var cell1 = row.insertCell(1);
                var cell2 = row.insertCell(2);
                cell0.innerHTML = gameStats[i].username;
                cell1.innerHTML = finalTime;
                cell2.innerHTML = gameStats[i].numClicks;
            }
            statsForm.appendChild(statsTable);

            statsForm.className = "form";
        }


    });
}());