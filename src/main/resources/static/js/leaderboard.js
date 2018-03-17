(function () {
    "use strict"
    window.addEventListener('load', function() {

        // Calls the api to get the list of game and populates the dropdown menu
        api.getGameList(function(err, gameList){
            if(err) console.log(err);
            else{
                var dropdown = document.querySelector('#select_game_dropdown');
                for(var index = 0; index < gameList.length; index++){
                    var opt = document.createElement("option");
                    opt.value = gameList[index];
                    opt.innerHTML = gameList[index];

                    dropdown.appendChild(opt);
                }
            }
        });

        // add event listener for changing dropdown menu for selecting game to view
        document.getElementById('select_game_dropdown').addEventListener('change', function(e){
            // not default, grab the game with the selected game id
            if(document.getElementById('select_game_dropdown').value != 'default'){
                selectedGame = document.getElementById('select_game_dropdown').value;
                // gets the game stats of the selected game id TODO
//                api.getGameStats(selectedGame, function(err, gameStats){
//                    if(err) console.log(err);
//                    else{
//                      // populate game stats
//                    }
//                });
            }
        });

    });
}());