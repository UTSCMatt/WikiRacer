/*jshint esversion: 6 */
(function () {
    "use strict";

    window.addEventListener("load", function () {

        var username = api.getUser();
        var showNonFinished = false;
        var offset = 0;
        var limit = 10;
        var query = window.location.hash.substring(1);
        var profileName = document.getElementById("profile_uname");
        var profileWrapper = document.getElementById("profile_wrapper");
        var hideProfileContainer = document.getElementById("hide_profile_container");

        if (!username) {
            profileWrapper.innerHTML = `You are not logged in`;
            hideProfileContainer.className = '';

        } else if (query == username || query == '') {

            api.userGames(username, false, offset, limit, function (err, res) {
                if (err) {
                    console.log(err);
                    alert(err);
                }
                else if (res.match) {

                    profileName.innerHTML = username;

                    var uploadForm = document.getElementById("img_form");

                    // uploads the given image to server
                    uploadForm.addEventListener('submit', function (e) {
                        e.preventDefault();
                        var imgFile = document.getElementById("file_upload").files[0];
                        uploadForm.reset();
                        api.postProfilePic(imgFile, function (err, res) {
                            if (err) {
                                console.log(err);
                                alert(err);
                            } else {
                                window.location.href = "profile.html";
                            }
                        });
                    });

                    var deleteButton = document.getElementById("delete_btn");
                    // deletes the image from the profile
                    deleteButton.addEventListener('click', function (e) {
                        api.deleteProfilePic(function (err, res) {
                            if (err) {
                                console.log(err);
                                alert(err);
                            } else {
                                window.location.href = "profile.html";
                            }
                        });
                    });

                    var toggleButton = document.getElementById("toggle_hidden_btn");
                    // adds script to button that pops up modal
                    toggleButton.addEventListener('click', function (e) {
                        var modal = document.getElementById("upload_modal");
                        modal.style.display = "block";
                        document.getElementsByClassName("close")[0].addEventListener('click', function (e) {
                            modal.style.display = "none";
                        });
                    });

                    toggleButton.className = 'btn';

                    var profilePic = document.getElementById("profile_pic");
                    profilePic.src = `/profile/` + username + `/image/`;
                    profilePic.onerror = function () {
                        this.src = "images/profile_placeholder.png";
                    };
                    addPageButtons(username);
                    listGames(res.games);
                    hideProfileContainer.className = '';
                }
            });

        } else {
            api.userGames(query, false, offset, limit, function (err, res) {
                if (err) {
                    console.log(err);
                    alert(err);
                } else {
                    profileName.innerHTML = query;

                    var profilePic = document.getElementById("profile_pic");
                    profilePic.src = `/profile/` + query + `/image/`;
                    profilePic.onerror = function () {
                        this.src = "images/profile_placeholder.png";
                    };
                    addPageButtons(query);
                    listGames(res.games);
                    hideProfileContainer.className = '';
                }
            });
        }

        function addPageButtons(name) {
            // previous page button
            document.getElementById("prev_page_btn").addEventListener('click', function (e) {
                // checking to see if a previous page exists
                if (offset - limit >= 0) {
                    offset = offset - limit;
                    api.userGames(name, false, offset, limit, function (err, games) {
                        if (err) console.log(err);
                        else {
                            listGames(games.games);
                        }
                    });
                } else {
                    alert("You are on the first page");
                }
            });
            // next page button
            document.getElementById("next_page_btn").addEventListener('click', function (e) {
                offset = offset + limit;
                api.userGames(name, false, offset, limit, function (err, games) {
                    if (err) console.log(err);
                    // checking if there is another page of games
                    else if (games.games.length === 0) {
                        offset = offset - limit;
                        alert("No Results");
                    } else {
                        listGames(games.games);
                    }
                });
            });
        }

        function addLinks(listElmt) {
            // gets the game stats when a game code is clicked
            listElmt.addEventListener('click', function (e) {
                api.getGameStats(listElmt.innerHTML, function (err, gameStats) {
                    if (err) console.log(err);
                    else {
                        displayGameTable(gameStats);
                    }
                });
            });
        }

        // shows a list of completed games by the user
        function listGames(gameData) {
            var listWrapper = document.getElementById("profile_list");
            listWrapper.innerHTML = ``;
            var list = document.createElement("ul");
            for (var i = 0; i < gameData.length; i++) {
                var listElement = document.createElement("li");
                listElement.className = "list_element";
                listElement.innerHTML = gameData[i];
                addLinks(listElement);
                list.appendChild(listElement);
            }
            listWrapper.appendChild(list);
        }

        function displayGameTable(data) {
            var statsForm = document.getElementById("profile_game_form");
            statsForm.innerHTML = '';
            // makes a table for the game configuration
            var configTable = document.createElement("table");
            configTable.id = "config_table";
            configTable.innerHTML = `<tr>
                                <th>Game Code</th>
                                <th>Start Page</th>
                                <th>End Page</th>
                                <th>Game Mode</th>
                                </tr>`;

            var row = configTable.insertRow(-1);
            var codeCell = row.insertCell(0);
            var startPageCell = row.insertCell(1);
            var endPageCell = row.insertCell(2);
            var modeCell = row.insertCell(3);
            codeCell.innerHTML = data[0].gameCode;
            startPageCell.innerHTML = data[0].startPage;
            endPageCell.innerHTML = data[0].endPage;
            modeCell.innerHTML = data[0].gameMode;

            // makes a table for the player stats of that game
            var statsTable = document.createElement("table");
            statsTable.id = "stats_table";
            statsTable.innerHTML = `<tr>
                                <th>Username</th>
                                <th>Time Taken</th>
                                <th>Clicks Taken</th>
                                </tr>`;

            var userGameData;
            // finds the stats only for the user in the profile page
            for (var i = 1; i < data.length; i++) {
                if ((query == '' && data[i].username == username) || data[i].username == query) {
                    userGameData = data[i];
                    break;
                }
            }
            var time = new Date(null);
            time.setSeconds(userGameData.timeSpend);
            var finalTime = time.toISOString().substr(11, 8);
            var statsRow = statsTable.insertRow(-1);
            var nameCell = statsRow.insertCell(0);
            var timeCell = statsRow.insertCell(1);
            var clicksCell = statsRow.insertCell(2);
            nameCell.innerHTML = userGameData.username;
            timeCell.innerHTML = finalTime;
            clicksCell.innerHTML = userGameData.numClicks;

            // displays path user took in game
            var pathTable = document.createElement("table");
            pathTable.innerHTML = `<tr>
                                <th>Path Taken</th>
                                </tr>`;

            var pathRow = pathTable.insertRow(-1);
            var pathCell = pathRow.insertCell(0);
            api.userGamePath(data[0].gameCode, userGameData.username, function (err, path) {
                if (err) console.log(err);
                else {
                    var pathNode = document.createTextNode(path);
                    pathCell.appendChild(pathNode);
                }
            });

            statsForm.appendChild(configTable);
            statsForm.appendChild(statsTable);
            statsForm.appendChild(pathTable);
            statsForm.className = "form";
        }

    });
})();