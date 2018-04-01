(function () {
    "use strict";
    window.addEventListener('load', function() {

        var formBox = document.getElementById("form_container");

        var gameReqs = {
            gameId: null,
            start: null,
            end: null,
            clicks: null,
            isSync: false
        };
        (function(){
            var gameForms = document.getElementById("form_container");
            var loginPrompt = document.getElementById("login_notify");
            // hides the game creation/join forms if the user is not logged in
            if (api.getUser()) {
                gameForms.className = "";
                loginPrompt.className = "hidden";
            } else {
                gameForms.className = "hidden";
                loginPrompt.className = "";
            }
        })();
              

        document.getElementById("start_btn").addEventListener("click", function (e) {
            e.preventDefault();
            // if the user enters a wikipedia url instead of the page title, splits the string and 
            // returns just the page title
            var startPage = document.getElementById("startpage").value.split("/wiki/").slice(-1)[0];
            var endPage = document.getElementById("endpage").value.split("/wiki/").slice(-1)[0];
            
            try {
                var gameMode = document.querySelector('input[name="game_mode"]:checked').value;
                var syncOpt = document.querySelector('input[name="game_sync"]').checked;
                var rulesCat = document.getElementById("ban_cat").value.replace(/, /g, ",");
                var rulesArt = document.getElementById("ban_art").value.replace(/, /g, ",");
                var rules = {
                    "categories" : [],
                    "articles" : []
                };
                // splits the user input by commas into an array
                rules.categories = rulesCat.split(",");
                rules.articles = rulesArt.split(",");
                formBox.innerHTML = `LOADING`;
                
                api.makeGame(startPage, endPage, gameMode, JSON.stringify(rules), syncOpt, function (err, res) {
                    if (err) {
                        console.log(err);
                        alert(err);
                        window.location.href = "game.html";
                    } 
                    else {
                        formBox.className = "form hidden";
                        gameReqs.gameId = res.id;
                        gameReqs.start = res.start;
                        gameReqs.end = res.end;
                        gameReqs.clicks = 0;
                        gameReqs.isSync = res.isSync;

                        if(gameReqs.isSync) {
                            makeLobby(gameReqs, true);
                        } else {
                            // get the json representation of the starting page
                            api.getWikiPage(gameReqs.start, function(err, res) {
                                if (err) console.log(err);
                                else createGameWindow(res, gameReqs);
                            });
                        }
                      
                    }
                });
            } catch (error) {
                alert("Please select a game mode");
            }
        });


        // used to toggle the options menu
        document.getElementById("options_btn").addEventListener("click", function(e) {
            var optionsBtn = document.getElementById("options_btn");
            var optionsMenu = document.getElementById("options_menu");
            if (optionsMenu.className == "hidden") {
                optionsMenu.className = "";
                optionsBtn.innerHTML = "Hide Options";
            } else {
                optionsMenu.className = "hidden";
                optionsBtn.innerHTML = "Show Options";
            }   
        });
        
        document.getElementById("join_btn").addEventListener("click", function (e) {
            e.preventDefault();
            var joinGameId = document.getElementById("gamecode").value;
            formBox.innerHTML = `LOADING`;

            // join existing game using game code
            api.joinGame(joinGameId, function (err, res) {
                if (err) {
                    console.log(err);
                    alert(err);
                    window.location.href = "game.html";
                } else {
                    formBox.className = "form hidden";
                    gameReqs.gameId = res.id;
                    gameReqs.start = res.start;
                    gameReqs.end = res.end;
                    gameReqs.clicks = 0;
                    gameReqs.isSync = res.isSync;
                    if (gameReqs.isSync) {
                        makeLobby(gameReqs);
                    }
                    else{
                        loadingScreen();
                        api.getWikiPage(gameReqs.start, function(err, res) {
                            if (err) console.log(err);
                            else createGameWindow(res, gameReqs);
                        });
                    }
                    
                }
            });
        });

        function makeLobby(gameReqs, isHost=false) {
            var lobbyBox = document.getElementById("lobbybox");  
            var lobbyForm = document.createElement("form");
            var buttonDiv = document.createElement("div");
            buttonDiv.id = "button_div";
            lobbyForm.className = "form";
            lobbyForm.id = "lobby_form";
            lobbyForm.innerHTML = `Game ID:` + gameReqs.gameId;
            // creates field to display users in lobby
            var userBox = document.createElement("fieldset");
            userBox.id = "user_box";
            userBox.innerHTML = `<legend>Users:</legend>`;

            // adds leave button
            var lobbyLeaveBtn = document.createElement("button");
            lobbyLeaveBtn.type = "button";
            lobbyLeaveBtn.className = "btn";
            lobbyLeaveBtn.id = "lobby_leave_btn";
            lobbyLeaveBtn.innerHTML = "Leave Lobby";
            lobbyLeaveBtn.addEventListener('click', function(e) {
                websocket.disconnect(gameReqs.gameId, function(err, res) {
                    if (err) console.log(err);
                    else {
                        window.location.href = "game.html";
                    }
                });
            });

            // adds start button if user is host
            if (isHost) {
                var lobbyStartBtn = document.createElement("button");
                lobbyStartBtn.type = "button";
                lobbyStartBtn.className = "btn";
                lobbyStartBtn.id = "lobby_start_btn";
                lobbyStartBtn.innerHTML = "Start Game";
                lobbyStartBtn.addEventListener('click', function(e) {
                    buttonDiv.className = "hidden";
                    loadingScreen();
                    api.startSyncGame(gameReqs.gameId, function(err, res) {
                        if (err) console.log(err);
                    })  
                });
                buttonDiv.appendChild(lobbyStartBtn);
            }

            buttonDiv.appendChild(lobbyLeaveBtn);
            

            // generates a table for storing player progress
            var userTable = document.createElement("table");
            userTable.id = "user_table";
        
            var userRow = document.createElement("tr");
            userRow.id = "user_row";
            userRow.innerHTML = `<th>Players:</th>`

            var clicksRow = document.createElement("tr");
            clicksRow.id = "clicks_row";
            clicksRow.innerHTML = `<th>Clicks:</th>`

            var statusRow = document.createElement("tr");
            statusRow.id = "status_row";
            statusRow.innerHTML = "<th>Status:</th>";

            userTable.appendChild(userRow);
            userTable.appendChild(clicksRow);
            userTable.appendChild(statusRow);

            // gets list of users from backend, displays in lobby 
            var lobbyUsers = api.getLobbyUsers(gameReqs.gameId, function(err, users) {
                if (err) console.log(err);
                else {
                  
                    for (var i = 0; i < users.length; i++) {
                        // adds table of users to show in lobby
                        // and their clicks count
                        var newUserCell = document.createElement("td");
                        var newClicksCell = document.createElement("td");
                        var newStatusCell = document.createElement("td");
                        var userNode = document.createTextNode(users[i]);
                        var clicksNode = document.createTextNode("0");
                        var statusNode = document.createTextNode("In Progress");

                        newUserCell.className = users[i];
                        newClicksCell.className = users[i];
                        newStatusCell.className = users[i];

                        newUserCell.appendChild(userNode);
                        newClicksCell.appendChild(clicksNode);
                        newStatusCell.appendChild(statusNode);

                        userRow.appendChild(newUserCell);
                        clicksRow.appendChild(newClicksCell);
                        statusRow.appendChild(newStatusCell);

                    }
                    userBox.appendChild(userTable);
                }
            });
            
            lobbyForm.appendChild(userBox);
            lobbyForm.appendChild(buttonDiv);
            lobbyBox.appendChild(lobbyForm);

            websocket.connect(gameReqs.gameId, receiveWebsocket);

        }

        function receiveWebsocket(socketInfo) {
            var socketProps = JSON.parse(socketInfo.body);
            var userTable = document.getElementById("user_table");
            var userRow = document.getElementById("user_row");
            var clicksRow = document.getElementById("clicks_row");
            var statusRow = document.getElementById("status_row");
            if (socketProps.joined) {
                // add newly joined user to lobby list
                var newUserCell = document.createElement("td");
                var newClicksCell = document.createElement("td");
                var newStatusCell = document.createElement("td");

                var userNode = document.createTextNode(socketProps.joined);
                var clicksNode = document.createTextNode("0");
                var statusNode = document.createTextNode("In Progress");

                newUserCell.className = socketProps.joined;
                newClicksCell.className = socketProps.joined;
                newStatusCell.className = socketProps.joined;

                newUserCell.appendChild(userNode);
                newClicksCell.appendChild(clicksNode);
                newStatusCell.appendChild(statusNode);

                userRow.appendChild(newUserCell);
                clicksRow.appendChild(newClicksCell);
                statusRow.appendChild(newStatusCell);
            }

            if (socketProps.left) {
                try {
                    // removes user name from lobby is user leaves
                    var delCells = document.getElementsByClassName(socketProps.left);
                    userRow.removeChild(delCells[0]);
                    clicksRow.removeChild(delCells[0]);
                    statusRow.removeChild(delCells[0]);
                } catch (error) {
                    console.log(error);
                }
            }

            if (socketProps.lobby_closed || socketProps.timed_out) {
                websocket.clientDisconnect(gameReqs.gameId);
                alert("Host has left the game, lobby has been closed");
                window.location.href = "game.html";
            }

            if (socketProps.player) {
                var updateCells = document.getElementsByClassName(socketProps.player);

                // update the clicks counter when a user clicks a new link
                var updatedClicks = document.createTextNode(socketProps.clicks);
                updateCells[1].innerHTML = '';
                updateCells[1].appendChild(updatedClicks);
                if (socketProps.finished) {
                    
                    // converts time in seconds to hh:mm:ss
                    var time = new Date(null);
                    time.setSeconds(socketProps.time);
                    var finalTime = time.toISOString().substr(11, 8);
                    
                    // update finished status
                    var updatedFinish = document.createTextNode("Finished in: " + finalTime);
                    updateCells[2].innerHTML = '';
                    updateCells[2].appendChild(updatedFinish);
                    
                }
            }

            if (socketProps.game_finished) {
                // displays pop up modal box 
                var modal = document.getElementById("finished_modal");
                var modalBtn = document.getElementById("proceed_btn");
                document.getElementById("modal_text").innerHTML = `All players have finished the game.
                                                                    <br> Click OK to go leaderboard.`;
                modalBtn.addEventListener('click', function(e) {
                    window.location.href = "leaderboard.html#" + gameReqs.gameId;
                });
                modal.style.display = "block";

                var rankingTable = document.createElement('table');
                rankingTable.id = "ranking_table";
                rankingTable.className = "table";
                rankingTable.innerHTML = `<tr>
                                    <th>Rank</th>
                                    <th>Player</th>
                                    <th>Clicks</th>
                                    <th>Time</th>
                                    <th>Path</th>
                                    </tr>`;
                var  rankingArray = socketProps.rankings;
                for (var i = 0; i < rankingArray.length; i++) {
                    var currentPlayer = rankingArray[i].player;
                    var currentPlayerStats = socketProps.player_info[currentPlayer];
                    var time = new Date(null);
                    time.setSeconds(currentPlayerStats.time);
                    var finalTime = time.toISOString().substr(11, 8);
                    var row = statsTable.insertRow(-1);
                    var rankCell = row.insertCell(0);
                    var playerCell = row.insertCell(1);
                    var clicksCell = row.insertCell(2);
                    var timeCell = row.insertCell(3);
                    var pathCell = row.insertCell(4);
                    rankCell.innerHTML = i
                    playerCell.innerHTML = currentPlayer;
                    clicksCell.innerHTML = currentPlayerStats.clicks;
                    timeCell.innerHTML = finalTime;
                    pathCell.innerHTML = currentPlayerStats.path;
                }
                modal.innerHTML += ranking_table;

            }

            if (socketProps.started) {
                var buttonDiv = document.getElementById("button_div");
                buttonDiv.className = "hidden";
                loadingScreen();
               
                api.getWikiPage(gameReqs.start, function(err, res) {
                    if (err) console.log(err);
                    else createGameWindow(res, gameReqs);
                });
                      
            }
        };


        // creates the structure for displaying the game
        function createGameWindow(content, gameReqs) {
            var gameBox = document.getElementById("gamebox");
            gameBox.innerHTML = "";
            var frameWrapper = document.createElement("div");
            var frame = document.createElement("div");
            frameWrapper.id = "framewrapper";
            frame.id = "wikiframe";
            frame.innerHTML = content.parse.text["*"];

            // hides the game window until all links and images have been added 
            frameWrapper.style.visibility = "hidden";
            frameWrapper.appendChild(frame);
            gameBox.appendChild(frameWrapper);
            gameBox.prepend("Game ID: " + gameReqs.gameId);
            gameBox.prepend("Start Page: " + gameReqs.start + " End Page: " + gameReqs.end + " Clicks: " + gameReqs.clicks + " ");
            insertLinks(content, gameReqs);
            frameWrapper.style.visibility = "";
        }

        function loadingScreen() {
            // replaces game window with loading screen
            var gameBox = document.getElementById("gamebox");
            gameBox.innerHTML = "";

            var frameWrapper = document.createElement("div");
            frameWrapper.id = "framewrapper";
            frameWrapper.style.backgroundColor = "#333333";

            var loadIcon = document.createElement("img");
            loadIcon.id = "load_icon";
            loadIcon.style.marginTop = "64px";
            loadIcon.src = "images/loader.gif";

            frameWrapper.innerHTML = "";
            frameWrapper.appendChild(loadIcon);
            gameBox.appendChild(frameWrapper);
        }

        function insertLinks(content, gameReqs) {
            var frameWrapper = document.getElementById("framewrapper");
            var links = frameWrapper.getElementsByTagName("a");
            var regex = new RegExp(/Template|#|action=edit|:|external/);
            function linksAdder(i) {
                var linkSplit = links[i].href.split("/wiki/").slice(-1)[0];
                // removes external links and template links
                if(regex.test(linkSplit) || regex.test(links[i].className)) {
                    links[i].removeAttribute("href");
                }
                // replaces internal wiki links with api calls for their respective pages
                else {
                    links[i].addEventListener('click', function (e) {
                        e.preventDefault();
                        try {
                            loadingScreen();
                        } catch (error) {
                            return error;
                        }
                        api.checkNewPage(gameReqs.gameId, linkSplit, function (err, res) {
                            if (err) {
                                console.log(err);
                                alert(err);
                                // returns the user to page they were on if the next page is banned or invalid
                                createGameWindow(content, gameReqs);
                            }
                            else if (res.finished) {
                                // placeholder for actual win 
                                var time = new Date(null);
                                time.setSeconds(res.time);
                                var finalTime = time.toISOString().substr(11, 8);

                                if (res.isSync) {
                                    var frameWrapper = document.getElementById("framewrapper");
                                    frameWrapper.style.color = "white";
                                    frameWrapper.innerHTML = `You've reached your destination! Your score is: <br> Clicks: `
                                                            + res.clicks + `<br> Time:` 
                                                            + finalTime + `<br> Waiting on other players to finish.`;
                                } else {
                                    var modal = document.getElementById("finished_modal");
                                    var modalBtn = document.getElementById("proceed_btn");
                                    document.getElementById("modal_text").innerHTML = `You've reached your destination! Your score is: <br> Clicks: `
                                                                                    + res.clicks + `<br> Time:` 
                                                                                    + finalTime + `<br> Click OK to go leaderboard.`;
                                    modalBtn.addEventListener('click', function(e) {
                                        window.location.href = "leaderboard.html#" + gameReqs.gameId;
                                    });
                                    modal.style.display = "block";
                                }
                                
                            } else {
                                gameReqs.clicks = res.clicks;
                                api.getWikiPage(res.current_page, function (err, res) {
                                    if (err) console.log(err);
                                    else {
                                        createGameWindow(res, gameReqs);
                                    }
                                });
                            }
                        });

                    });
                }
               
            }
            for (var i = 0; i < links.length; i++) {
                linksAdder(i);
            }
            
        }
        
    });
}());