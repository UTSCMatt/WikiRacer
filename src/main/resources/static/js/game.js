(function () {
    "use strict"
    window.addEventListener('load', function() {
        
        var newGameForm = document.getElementById("new_game_form");
        var joinGameForm = document.getElementById("join_game_form");
        var formBox = document.getElementById("form_container");

        document.getElementById("start_btn").addEventListener("click", function (e) {
            e.preventDefault();
            // if the user enters a wikipedia url instead of the page title, splits the string and 
            // returns just the page title
            var startPage = document.getElementById("startpage").value.split("/wiki/").slice(-1)[0];
            var endPage = document.getElementById("endpage").value.split("/wiki/").slice(-1)[0];
            
            try {
                //var gameMode = document.querySelector('input[name="game_mode"]:checked').value;
                api.makeGame(startPage, endPage/*, gameMode, "test"*/, function (err, res) {
                    if (err) console.log(err);
                    else {
                        formBox.style.display = "none";
                        startPage = res.start;
                        endPage = res.end;
                        var gameId = res.id;
                        // get the json representation of the starting page
                        api.getWikiPage(startPage, function(err, res) {
                            if (err) console.log(err);
                            else processNewPage(res, gameId);
                        });
                    }
                });
            } catch (error) {
                alert("Please select a game mode");
            }
        });

        document.getElementById("options_btn").addEventListener("click", function(e) {
            var optionsBtn = document.getElementById("options_btn");
            var optionsMenu = document.getElementById("options_menu");
            if (optionsMenu) {
                if (optionsMenu.style.display == "none") {
                    optionsMenu.style.display = "";
                    optionsBtn.innerHTML = "Hide Options"
                } else {
                    optionsMenu.style.display = "none";
                    optionsBtn.innerHTML = "Show Options"
                }
            } else {
                var optionsMenu = document.createElement("div");
                optionsMenu.id = "options_menu";
                optionsMenu.innerHTML = `<input type="radio" id="opt_time" name="game_mode" value="Timed"> 
                                    Time based scoring<br>
                                    <input type="radio" id="opt_clicks" name="game_mode" value="Clicks">
                                    Click based scoring`;
                document.getElementById("options_wrapper").appendChild(optionsMenu);
                optionsBtn.innerHTML = "Hide Options"
                
            }
            
        });

        document.getElementById("join_btn").addEventListener("click", function (e) {
            e.preventDefault();
            var joinGameId = document.getElementById("gamecode").value;
            api.joinGame(joinGameId, function (err, res) {
                if (err) console.log(err);
                else {
                    formBox.style.display = "none";
                    
                    processNewPage(res);
                }
            });
        });

        function processNewPage(content, gameId) {
            var pageObj = JSON.parse(content);
            createGameWindow(pageObj, gameId);
        };

        // creates the structure for displaying the game
        function createGameWindow(content, gameId) {
            var gameBox = document.getElementById("gamebox");
            var frameWrapper = document.createElement("div");
            var frame = document.createElement("div");
            frameWrapper.id = "framewrapper";
            frame.id = "wikiframe";
            frame.innerHTML = content.parse.text["*"];
            // hides the game window until all links and images have been added 
            frameWrapper.style.visibility = "hidden";
            frameWrapper.appendChild(frame);
            gameBox.appendChild(frameWrapper);
            gameBox.prepend("Game ID: " + gameId);
            insertLinks();
            insertImages();
            frameWrapper.style.visibility = "";
        };

        function insertLinks() {

            var links = document.getElementsByTagName("a");

            for (var i = 0; i < links.length; i++) {
                (function () {
                    // removes external links and template links
                    if((links[i].href.split("/wiki/")[1].includes("Template")) || links[i].className == "external text") {
                        links[i].removeAttribute("href");
                    }
                    // replaces internal wiki links with api calls for their respective pages
                    else {
                        links[i].addEventListener('click', function (e) {
                            e.preventDefault();
                            api.getWikiPage(links[i].href.split("/wiki/")[1], function(err, res) {
                                if (err) console.log(err);
                                else {
                                    processNewPage(res);
                                }
                            });   
                        });
                    }
                   
                })();
            }
            
        };
        // retrieves images from wiki and inserts them
        function insertImages() {

        };

        
    });
}());