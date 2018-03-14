(function() {
    "use strict"
    window.onload = function() {
        var navBar = document.getElementById("navbar");
        var newElmt = this.document.createElement('li');
        newElmt.className = "navelmt";
        newElmt.id = "loginBtn";
        if (!api.getUser()){
            newElmt.innerHTML = `<a href="login.html">Login/Signup</a>`
        } else {
            newElmt.innerHTML = `<a>Logout</a>`
            newElmt.addEventListener('click', function(){
                api.signout(function(err, res){
                    if (err) console.log(err);
                    else window.location.href = "";
                });
            });
        }
        navBar.appendChild(newElmt);
    };
}());