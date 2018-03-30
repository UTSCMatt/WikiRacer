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

  return module;

})();