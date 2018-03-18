(function () {
    "use strict"
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
                else if (games.length == 0){
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
                    api.getGameStats(listElmt.innerHTML, function (err, gameStats) {
                        if (err) console.log(err);
                        else {
                            renderStats(gameStats);
                        }
                    });
                });
            };
            // appends each game code to the list
            for (var index = 0; index < games.length; index++) {
                var listElmt = document.createElement("li");
                listElmt.className = "list_element";
                listElmt.innerHTML = games[index];
                addLinks(listElmt);
                gameList.appendChild(listElmt);
            }
            
        };

        function renderStats(gameStats) {
  
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
            for (var i = 0; i < gameStats.length; i++) {
                var row = statsTable.insertRow(-1);
                var cell0 = row.insertCell(0);
                var cell1 = row.insertCell(1);
                var cell2 = row.insertCell(2);
                cell0.innerHTML = gameStats[i]["username"];
                cell1.innerHTML = gameStats[i]["timeSpend"];
                cell2.innerHTML = gameStats[i]["numClicks"];
            };
            statsForm.appendChild(statsTable);

            statsForm.className = "form";
        };


    });
}());