const socketManager = (() => {
    const sockets = {}; // Store sockets by user/session ID

    function connect(userId) {
        if (!sockets[userId]) {
            const socket = new WebSocket('wss://chat.atrons.org/');

            socket.onopen = () => {
                console.log(`Connected to the chat server for user ${userId}.`);
            };

            socket.onmessage = (event) => {
                // Handle incoming messages as before
                if (event.data instanceof Blob) {
                    const reader = new FileReader();
                    reader.onload = function() {
                        const message = reader.result;
                        displayMessage(message);
                    };
                    reader.readAsText(event.data);
                } else {
                    displayMessage(event.data);
                }
            };

            socket.onclose = () => {
                console.log(`Disconnected from the chat server for user ${userId}.`);
                delete sockets[userId]; // Remove the socket on close
            };

            sockets[userId] = socket; // Store the new socket
        }
    }

    function sendMessage(userId, message) {
        if (sockets[userId]) {
            sockets[userId].send(message); // Send message using the specific socket
        } else {
            console.log(`No active socket for user ${userId}.`);
        }
    }

    function displayMessage(message) {
        const messageDisplay = document.getElementById('messages');
        messageDisplay.innerHTML += `<p>${message}</p>`;
    }

    return { connect, sendMessage };
})();

// Usage
const userId = 'user123'; // Replace with a real user ID or session ID
socketManager.connect(userId);

// Update sendMessage function
function sendMessage() {
    const name = document.getElementById('nameInput').value;
    const message = messageInput.value;

    if (message) {
        const fullMessage = `${name}: ${message}`;
        socketManager.sendMessage(userId, fullMessage);
        messageInput.value = ''; // Clear the input field
    }
}
socket.onerror = (error) => {
    console.error('WebSocket Error: ', error);
    alert('An error occurred with the chat connection. Please try again.');
};

