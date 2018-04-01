(function () {
    "use strict";
    
    window.addEventListener("load", function() {

        var username = api.getUser();
        var query = window.location.hash.substring(1);

        var profileName = document.getElementById("profile_uname");
        if (!username) {
            profileName.innerHTML = "You are not logged in";

        } else if (query == username || query == '') {
            
            profileName.innerHTML = username;
            var toggleButton = document.getElementById("toggle_hidden_btn");
            toggleButton.addEventListener('click', function(e){
                var imgFormWrapper = document.getElementById("img_form_wrapper");
                switch (imgFormWrapper.className) {
                    // toggles the display of the image submission form
                    case '':
                        imgFormWrapper.className = 'hidden';
                        break;
                    case 'hidden':
                        imgFormWrapper.className = '';
                        break;
                }
            });

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
                    }
                });
            });
            toggleButton.className = 'btn';
            
            var profilePic = document.getElementById("profile_pic");
            profilePic.src = `/profile/` + username + `/image/`;

        } else {
            getProfileByName(query);
        }

        // renders a requested profile that is not the current user's
        function getProfileByName(profileName) {
            
        };

        // renders a list of the user's completed games
        function generateGamesList(data) {

        };

        // renders a table showing a game's statistics
        function generateGamesTable(data) {

        };
        
    });
})();