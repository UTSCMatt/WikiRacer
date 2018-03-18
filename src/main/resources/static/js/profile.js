(function () {
    "use strict";
    
    window.addEventListener("load", function() {
        var username = api.getUser();
        var profileName = document.getElementById("profile_uname");
        if (!username) {
            profileName.innerHTML = "You are not logged in";
        } else {
            username = api.getUser();
            profileName.innerHTML = "Your username is: " + username;
        }
        
    });
})();