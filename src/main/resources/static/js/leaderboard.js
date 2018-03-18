(function () {
    "use strict"
    window.addEventListener('load', function() {
        
        var offset = 0;
        var limit = 10;

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
                else if (games == []){
                    offset = offset - limit;
                } else {
                    getPage(games);
                }
            });
        });

        function getPage (games) {
            var gameList = document.querySelector('.game_list');
            document.getElementById("stats_form").className = "hidden";
            
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
            document.getElementById("select_game_form").className = "form";
        };
        getPage(offset, limit);

        function renderStats() {
            document.getElementById("select_game_form").className = "hidden";
            var statsForm = document.getElementById("stats_form");
            statsForm.innerHTML = "";
            var returnBtn = document.createElement("button");
            returnBtn.className = "btn";
            returnBtn.id = "return_btn";
            returnBtn.innerHTML = "Return";

            var statsTable = document.createElement("table");
            statsTable.id = "stats_table";
            statsTable.className = "table";
            

            statsForm.className = "form";
        };


    });
}());