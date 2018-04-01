(function () {
    "use strict";
    
    window.addEventListener("load", function() {

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
            
            api.userGames(username, false, offset, limit, function(err, res) {
                if (err) {
                    console.log(err);
                    alert(err);
                }
                else if (res.match) {

                    profileName.innerHTML = username;

                    var uploadForm = document.getElementById("img_form");

                    // uploads the given image to server
                    uploadForm.addEventListener('submit', function(e) {
                        e.preventDefault();
                        var imgFile = document.getElementById("file_upload").files[0];
                        uploadForm.reset();
                        api.postProfilePic(imgFile, function(err, res) {
                            if (err) {
                                console.log(err);
                                alert(err);
                            } else {
                                window.location.href = "profile.html";
                            }
                        });
                    });

                    var deleteButton = document.getElementById("del_btn");
                    deleteButton.addEventListener('click', function(e) {
                        api.deleteProfilePic(function(err, res) {
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
                    toggleButton.addEventListener('click', function(e) {
                        var modal = document.getElementById("upload_modal");
                        modal.style.display = "block";
                        document.getElementsByClassName("close")[0].addEventListener('click', function(e) {
                            modal.style.display = "none";
                        });
                    });

                    toggleButton.className = 'btn';
                    
                    var profilePic = document.getElementById("profile_pic");
                    profilePic.src = `/profile/` + username + `/image/`;

                    listGames(res.games);
                    hideProfileContainer.className = '';
                }
            });
            
        } else {
            api.userGames(query, false, offset, limit, function(err, res) {
                if (err) {
                    console.log(err);
                    alert(err);
                } else {
                    profileName.innerHTML = query;

                    var profilePic = document.getElementById("profile_pic");
                    profilePic.src = `/profile/` + query + `/image/`;
                    
                    listGames(res.games);
                    hideProfileContainer.className = '';
                }
            });
        }
      
        // shows a list of completed games by the user
        function listGames(gameData) {
            var listWrapper = document.getElementById("profile_list");
            var list = document.createElement("ul");
            for (var i = 0; i < gameData.length; i++) {
                var listElement = document.createElement("li");
                listElement.className = "list_element";
                listElement.innerHTML = gameData[i];
                list.appendChild(listElement);
            }
            listWrapper.appendChild(list);
        };
        
    });
})();