var api = (function () {

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

        var xhr = new XMLHttpRequest();
        xhr.onload = function () {
            if (xhr.status !== 200) callback("[" + xhr.status + "]" + xhr.responseText, null);
            else callback(null, JSON.parse(xhr.responseText));
        };

        xhr.open(method, url, true);
        if (!data) xhr.send();
        else {
            var formdata = new FormData();
            Object.keys(data).forEach(function (key) {
                var value = data[key];
                formdata.append(key, value);
            });

            xhr.send(formdata);
        }

    }
    var module = {};

    // reads cookie and returns the username of current user
    module.getUser = function () {
        var cookies = document.cookie;
        var username = cookies.split("username=")[1];
        return username;
    };

    module.signup = function (username, password, callback) {
        send("POST", "/signup/", { username: username, password: password }, callback);
    };

    module.signin = function (username, password, callback) {
        send("POST", "/login/", { username: username, password: password }, callback);
    };

    module.signout = function (callback) {
        send("GET", "/logoff/", null, callback);
    };

    module.makeGame = function (start, end, gameMode, rules, syncOpt, callback) {
        send("POST", "/api/game/new/", { start: start, end: end, gameMode: gameMode, rules: rules, isSync: syncOpt }, callback);
    };

    module.joinGame = function (gameId, callback) {
        send("POST", "/api/game/join/", { gameId: gameId }, callback);
    };
    // queries mediawiki api for the page content and properties
    module.getWikiPage = function (query, callback) {
        sendWikiApi("GET", wikiURL + "?action=parse&format=json&page=" + query + "&origin=*&prop=text%7Clinks%7Ctemplates%7Cimages%7Csections%7Cdisplaytitle%7Ciwlinks%7Cproperties%7Cparsewarnings", null, callback);
    };

    module.checkNewPage = function (gameId, nextPage, callback) {
        send("POST", "/api/game/" + gameId + "/goto/", { nextPage: nextPage }, callback);
    };

    module.getGameList = function (offset, limit, search, callback) {
        var url = "/api/gameList?offset=" + offset + "&limit=" + limit;
        if (search) {
            url += "&search=" + search;
        }
        send("GET", url, null, callback);
    };

    module.getGameStats = function (gameId, callback) {
        send("GET", "/api/game/" + gameId + "/stats/", null, callback);
    };

    module.startSyncGame = function (gameId, callback) {
        send("PATCH", "/api/game/realtime/" + gameId + "/start/", null, callback);
    };

    module.leaveSyncGame = function (gameId, callback) {
        send("GET", "/api/game/" + gameId + "/leave/", null, callback);
    };

    module.getLobbyUsers = function (gameId, callback) {
        send("GET", "/api/game/realtime/" + gameId + "/players/", null, callback);
    };

    module.postProfilePic = function (file, callback) {
        send("POST", "/api/profile/image/", { file: file }, callback);
    };

    module.deleteProfilePic = function (callback) {
        send("DELETE", "/api/profile/image/", null, callback);
    };

    module.getProfileData = function (user, callback) {
        send("GET", "/api/user/" + user + "/game/", null, callback);
    };

    module.userGames = function (username, showNonFinished, offset, limit, callback) {
        send("GET", "/api/user/" + username + "/game?showNonFinished=" + showNonFinished + "&offset=" + offset + "&limit=" + limit, null, callback);
    };

    module.userGamePath = function (gameId, username, callback) {
        send("GET", "/api/game/" + gameId + "/player/" + username + "/path/", null, callback);
    };

    module.topStartEndPage = function (limit, callback) {
        send("GET", "/api/article/mostused?limit=" + limit, null, callback);
    };

    module.sendMessage = function (gameId, messageContent, callback) {
        send("POST", "/api/game/realtime/" + gameId + "/message/", { messageContent: messageContent }, callback);
    };
    return module;

})();