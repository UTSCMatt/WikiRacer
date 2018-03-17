var api = (function() {

    var wikiURL = "https://en.wikipedia.org/w/api.php";
   
    // send cross origin requests to mediawiki api
    function sendWikiApi(method, url, data, callback) {
        var xhr = new XMLHttpRequest();
        xhr.onload = function () {
            if (xhr.status !== 200) callback("[" + xhr.status + "]" + xhr.responseText, null);
            else callback(null, JSON.parse(xhr.responseText));
        };
        xhr.open(method, url, true);
        if (!data) xhr.send();
        else { // converts data to json if there is any
            xhr.setRequestHeader("Origin", "*");
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.send(JSON.stringify(data));
        }
    }
    // send data through Ajax requests
    function send(method, url, data, callback) {
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

    // reads cookie and returns the username of current user
    module.getUser = function(){
        var cookies = document.cookie;
        var username = cookies.split("username=")[1];
        return username;
    };

    module.signup = function(username, password, callback) {
        send("POST", "/signup/", {username: username, password: password}, callback);
    };

    module.signin = function(username, password, callback) {
        send("POST", "/login/", {username: username, password: password}, callback);
    };

    module.signout = function(callback) {
        send("GET", "/logoff/", null, callback);
    };

    module.makeGame = function(start, end, /*gameMode, rules,*/ callback) {
        send("POST", "/api/game/new/", {start: start, end: end/*, gameMode: gameMode, rules: rules*/}, callback);
    };

    module.joinGame = function(gameId, callback) {
        send("POST", "/api/game/join/", {gameId: gameId}, callback);
    };

    module.getWikiPage = function(query, callback) {
        send("GET", wikiURL + "?action=parse&format=json&page=" + query + "&prop=text%7Clinks%7Ctemplates%7Cimages%7Csections%7Cdisplaytitle%7Ciwlinks%7Cproperties%7Cparsewarnings", null, callback);
    };

    module.getImgUrl = function(filename, callback) {
    	send("GET", wikiURL + filename, null, callback);
    };

    module.getGameList = function(callback) {
    	send("GET", "/api/getGameList/", null, callback);
    };

    return module;

})();