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
                var selectedGame = document.getElementById('select_game_dropdown').value;
                // gets the game stats of the selected game id
                api.getGameStats(selectedGame, function(err, gameStats){
                    if(err) console.log(err);
                    else{
                      // populate game stats
                      removeTable();
                      generateTable(gameStats);

                    }
                });
            }
        });

        function generateTable(gameStats){
           var elmt = document.createElement('table');
           elmt.id = "leaderboardTable"
           elmt.className = "leaderboard";
           elmt.innerHTML=`<tr>
                               <th>Username</th>
                               <th>Time Spend (Seconds)</th>
                               <th>Number of Clicks</th>
                           </tr>`;
           for(var index = 0; index < gameStats.length; index++){
              // creates a row for each element
              var row = elmt.insertRow(-1);
              var usernameCell = row.insertCell(0)
              var timeSpendCell = row.insertCell(1)
              var numClicksCell = row.insertCell(2)
              usernameCell.innerHTML = gameStats[index][0];
              timeSpendCell.innerHTML = gameStats[index][1];
              numClicksCell.innerHTML = gameStats[index][2];
           }
           document.querySelector("#select_game_form").appendChild(elmt);
        }

        function removeTable(){
          var elmt = document.querySelector("#leaderboardTable");
          if(elmt){
            document.querySelector("#select_game_form").removeChild(elmt);
          }
        }

    });
}());