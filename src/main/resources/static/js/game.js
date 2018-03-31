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
                            makeLobby(gameReqs);
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
                } 
                else {
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

        function makeLobby(gameReqs) {
            var lobbyBox = document.getElementById("lobbybox");  
            var lobbyForm = document.createElement("form");
            var buttonDiv = document.createElement("div");
            lobbyForm.className = "form";
            lobbyForm.id = "lobby_form";
            lobbyForm.innerHTML = `Game ID:` + gameReqs.gameId;
            // creates field to display users in lobby
            var userBox = document.createElement("fieldset");
            userBox.id = "user_box";
            userBox.innerHTML = `<legend>Users:</legend>`;
            // adds start button
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
                    else {
                        api.getWikiPage(gameReqs.start, function(err, res) {
                            if (err) console.log(err);
                            else createGameWindow(res, gameReqs);
                        });
                    }
                })
               
            });
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

            var userList = document.createElement("ul");
            userList.id = "user_list";
            // gets list of users from backend, displays in lobby 
            var lobbyUsers = api.getLobbyUsers(gameReqs.gameId, function(err, users) {
                if (err) console.log(err);
                else {
                  
                    for (var i = 0; i < users.length; i++) {
                        // adds list of users to show in lobby
                        var userNode = document.createElement("li");
                        userNode.innerHTML = users[i];
                        userList.appendChild(userNode);
                    }
                    userBox.appendChild(userList);
                }
            });

            
            buttonDiv.appendChild(lobbyLeaveBtn);
            buttonDiv.appendChild(lobbyStartBtn);
            lobbyForm.appendChild(userBox);
            lobbyForm.appendChild(buttonDiv);
            lobbyBox.appendChild(lobbyForm);

            websocket.connect(gameReqs.gameId, receiveWebsocket);

        }

        function receiveWebsocket(socketInfo) {
            var socketProps = JSON.parse(socketInfo.body);
            if (socketProps.joined) {
                // add newly joined user to lobby list
                var userList = document.getElementById("user_list");
                var userNode = document.createElement("li");
                userNode.id = socketProps.joined;
                userNode.innerHTML = socketProps.joined;
                userList.prepend(userNode);
            }

            if (socketProps.left) {
                try {
                    // removes user name from lobby is user leaves
                    var userNode = document.getElementById(socketProps.left);
                    userNode.parentNode.removeChild(userNode);
                } catch (error) {
                    console.log(error);
                }
            }

            if (socketProps.lobby_closed || socketProps.timed_out) {
                websocket.clientDisconnect(gameReqs.gameId);
            }

            if (socketProps.player) {
                
            }

            if (socketProps.game_finished) {
                
            }

            if (socketProps.started) {
                
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
            loadIcon.style.marginTop = "64px";
            loadIcon.src = "images/loader.gif";
            frameWrapper.innerHTML = "";
            frameWrapper.appendChild(loadIcon);
            gameBox.appendChild(frameWrapper);
        }

        function insertLinks(content, gameReqs) {
        
            var links = document.getElementById("framewrapper").getElementsByTagName("a");
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
                                alert("You've reached your destination! Your score is: \n Clicks: " + res.clicks + "\n Time: " + finalTime);
                                window.location.href = "leaderboard.html#" + gameReqs.gameId;
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