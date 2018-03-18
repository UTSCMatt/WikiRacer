(function () {
    "use strict"
    window.addEventListener('load', function() {
        
        var offset = 0;
        var limit = 10;

        document.getElementById("prev_page_btn").addEventListener('click', function(e) {
            
        });

        function getPage (offset, limit) {
            // Calls the api to get the list of game and populates the list
            api.getGameList(offset, limit, function (err, games) {
                if (err) console.log(err);
                else {
                    function addLinks(listElmt) {
                        listElmt.addEventListener('click', function (e) {
                            api.getGameStats(listElmt.innerHTML, function (err, gameStats) {
                                if (err) console.log(err);
                                else {

                                }
                            });
                        });
                    };
                    var gameList = document.querySelector('.game_list');
                    for (var index = 0; index < games.length; index++) {
                        var listElmt = document.createElement("li");
                        listElmt.className = "list_element";
                        listElmt.innerHTML = games[index];
                        addLinks(listElmt);
                        gameList.appendChild(listElmt);
                    }
                }
            });
        };
        getPage(offset, limit);



    });
}());