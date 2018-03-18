(function () {
    "use strict"
    window.addEventListener('load', function() {
        
        var newGameForm = document.getElementById("new_game_form");
        var joinGameForm = document.getElementById("join_game_form");
        var formBox = document.getElementById("form_container");

        var gameReqs = {
            gameId: null,
            start: null,
            end: null,
        };

        document.getElementById("start_btn").addEventListener("click", function (e) {
            e.preventDefault();
            // if the user enters a wikipedia url instead of the page title, splits the string and 
            // returns just the page title
            var startPage = document.getElementById("startpage").value.split("/wiki/").slice(-1)[0];
            var endPage = document.getElementById("endpage").value.split("/wiki/").slice(-1)[0];
            
            try {
                var gameMode = document.querySelector('input[name="game_mode"]:checked').value;
                var rulesCat = document.getElementById("ban_cat").value.replace(/, /g, ",");
                var rulesArt = document.getElementById("ban_art").value.replace(/, /g, ",");
                var rules = {
                    "categories" : [],
                    "articles" : []
                };
                // splits the user input by commas into an array
                rules.categories = rulesCat.split(",");
                rules.articles = rulesArt.split(",");
                formBox.style.display = "none";
                loadingScreen();
                api.makeGame(startPage, endPage, gameMode, JSON.stringify(rules), function (err, res) {
                    if (err) console.log(err);
                    else {

                        gameReqs.gameId = res.id;
                        gameReqs.start = res.start;
                        gameReqs.end = res.end;
                        // get the json representation of the starting page
                        api.getWikiPage(gameReqs.start, function(err, res) {
                            if (err) console.log(err);
                            else createGameWindow(res, gameReqs);
                        });
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
                optionsBtn.innerHTML = "Hide Options"
            } else {
                optionsMenu.className = "hidden";
                optionsBtn.innerHTML = "Show Options"
            }   
        });
        
        document.getElementById("join_btn").addEventListener("click", function (e) {
            e.preventDefault();
            var joinGameId = document.getElementById("gamecode").value;
            formBox.style.display = "none";
            loadingScreen();
            // join existing game using game code
            api.joinGame(joinGameId, function (err, res) {
                if (err) console.log(err);
                else {
                    gameReqs.gameId = res.id;
                    gameReqs.start = res.start;
                    gameReqs.end = res.end;
                    api.getWikiPage(gameReqs.start, function(err, res) {
                        if (err) console.log(err);
                        else createGameWindow(res, gameReqs);
                    });
                }
            });
        });

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
            gameBox.prepend("Start Page: " + gameReqs.start + " End Page: " + gameReqs.end + " ");
            insertLinks(content, gameReqs);
            frameWrapper.style.visibility = "";
        };

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
        };

        function insertLinks(content, gameReqs) {
        
            var links = document.getElementById("framewrapper").getElementsByTagName("a");
            var regex = new RegExp(/Template|#|action=edit|:|external/);
            for (var i = 0; i < links.length; i++) {
                (function () {
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
                            api.checkNewPage(gameReqs.gameId, linkSplit, function(err, res) {
                                if (err) {
                                    console.log(err);
                                    alert("link not allowed");
                                    // returns the user to page they were on if the next page is banned or invalid
                                    createGameWindow(content, gameReqs);
                                }
                                else if (res.finished) {
                                     // placeholder for actual win 
                                     alert("a winner is you!");   
                                } else {
                                    api.getWikiPage(res.current_page, function(err, res) {
                                        if (err) console.log(err);
                                        else {
                                            createGameWindow(res, gameReqs);
                                        }
                                    });  
                                }
                            });
                            
                        });
                    }
                   
                })();
            }
            
        };
        
    });
}());