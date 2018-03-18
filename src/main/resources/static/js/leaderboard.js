(function () {
    "use strict"
    window.addEventListener('load', function() {
        
        var offset = 0;
        var limit = 10;

        api.getGameList(offset, limit, function(err, games) {
            if (err) console.log(err);
            else {
                getPage(games);
            }
        });

        document.getElementById("prev_page_btn").addEventListener('click', function(e) {
            if (offset - limit >= 0) {
                offset = offset - limit;
                api.getGameList(offset, limit, function(err, games){
                    if (err) console.log(err);
                    else {
                        getPage(games);
                    }
                });
            } 
        });

        document.getElementById("next_page_btn").addEventListener('click', function(e) {
            offset = offset + limit;
            api.getGameList(offset, limit, function(err, games) {
                if (err) console.log(err);
                else if (games.length == 0){
                    offset = offset - limit;
                } else {
                    getPage(games);
                }
            });
        });

        function getPage (games) {
            var gameList = document.querySelector('.game_list');
            gameList.innerHTML = "";
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

            var statsTable = document.createElement("table");
            statsTable.id = "stats_table";
            statsTable.className = "table";
            statsTable.innerHTML = `<tr>
                                <th>Username</th>
                                <th>Time Taken</th>
                                <th>Clicks Taken</th>
                                </tr>`;

            for (var i = 0; i < gameStats.length; i++) {
                var row = statsTable.insertRow(-1);
                var cell0 = row.insertCell(0);
                var cell1 = row.insertCell(1);
                var cell2 = row.insertCell(2);
                cell0.innerHTML = gameStats[i][0];
                cell1.innerHTML = gameStats[i][1];
                cell2.innerHTML = gameStats[i][2];
            };
            statsForm.appendChild(statsTable);

            statsForm.className = "form";
        };


    });
}());