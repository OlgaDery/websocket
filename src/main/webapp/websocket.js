var websocketSession;

function f_onmessage(evt) {
    var websocketMessages = document.getElementById('websocketMessages');
    websocketMessages.innerHTML = websocketMessages.innerHTML + evt.data + '<br/>';
}

function open(msg) {
    if (!websocketSession) {
        websocketSession = new WebSocket("ws://localhost:8080/websocket/websocket/"+ msg);
        websocketSession.onmessage = f_onmessage;
    }
}

function close() {
    if (websocketSession) {
        websocketSession.close();
        $('#websocketMessages').empty();
    }
}

function sendMessage(msg) {
    websocketSession.send(msg);
}