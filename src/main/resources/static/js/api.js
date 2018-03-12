var api = (function() {

    // used to send Ajax requests with json data encoding
    function send(method, url, data, callback) {
        var xhr = new XMLHttpRequest();
        xhr.onload = function () {
            if (xhr.status !== 200) callback("[" + xhr.status + "]" + xhr.responseText, null);
            else callback(null, JSON.parse(xhr.responseText));
        };
        xhr.open(method, url, true);
        if (!data) xhr.send();
        else { // converts data to json if there is any
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.send(JSON.stringify(data));
        }
    }
    // send files through Ajax requests
    function sendFiles(method, url, data, callback) {
        var formdata = new FormData();
        Object.keys(data).forEach(function (key) {
            var value = data[key];
            formdata.append(key, value);
        });
        var xhr = new XMLHttpRequest();
        xhr.onload = function () {
            if (xhr.status !== 200) callback("[" + xhr.status + "]" + xhr.responseText, null);
            else callback(null, JSON.parse(xhr.responseText));
        };
        xhr.open(method, url, true);
        xhr.send(formdata);
    }
    var module = {};
    module.getUser = function(){
        var cookies = document.cookie;
        var username = cookies.split("username=")[1];
        return username;
    };

    module.signup = function(username, password, callback) {
        send("POST", "/signup/", {username: username, password: password}, callback);
    };

    module.signin = function(username, password, callback) {
        send("POST", "/signin/", {username: username, password: password}, callback);
    };

    module.signout = function(callback) {
        send("POST", "/signout/", null, callback);
    };

    return module;

})();