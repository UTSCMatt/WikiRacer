(function(){
    "use strict";
    window.onload = function() {

        function submit() {
            if (document.querySelector("form").checkValidity()){
                // retrieves the values for username, password, and Login/Signup action
                var username = document.querySelector("form [name=username]").value;
                var password = document.querySelector("form [name=password]").value;
                var action = document.querySelector("form [name=action]").value;
                // changes the action sent to api depending on form input
                api[action](username, password, function(err, res) {
                    if (err) alert(err);
                    else window.location.href = '/';
                });
            };
        }
        // changes the intended action to signin
        document.getElementById("login").addEventListener('click', function(e) {
            document.querySelector("form [name=action]").value = "signin";
            submit();
        });
        // changes the intended action to signup
        document.getElementById("signup").addEventListener('click', function(e) {
            document.querySelector("form [name=action]").value = "signup";
            submit();
        });
        // stops default action of form submission from refreshing page
        document.querySelector('form').addEventListener('submit', function(e){
            e.preventDefault();
        });

    };
})();