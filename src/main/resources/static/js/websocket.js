var websocket = (function () {

    var stompClient = null;

    function connect(gameId, callback) {
        var socket = new SockJS('/wikiracer-websocket');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            stompClient.subscribe('/socket/' + gameId, callback);
        });
    }

    var module = {};

    module.connect = function (gameId, callback) {
        connect(gameId, callback);
    };

    module.disconnect = function (gameId, callback) {
        if (stompClient !== null) {
            api.leaveSyncGame(gameId, function (err, res) {
                if (err) callback(err, null);
                else {
                    stompClient.disconnect();
                    callback(null, res);
                }
            });
        }
    };

    module.clientDisconnect = function (gameId) {
        if (stompClient !== null) {
            stompClient.disconnect();
        }
    };

    return module;

})();