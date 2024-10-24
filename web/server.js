const https = require('https');
const fs = require('fs');
const path = require('path');
const WebSocket = require('ws');

const port = 8007;

// SSL options
const options = {
    key: fs.readFileSync('privateKey.key'), // Update with your key file
    cert: fs.readFileSync('certificate.crt'), // Update with your cert file
};

// Request handler to respond to HTTP requests
const requestHandler = (req, res) => {
    let filePath = path.join(__dirname, req.url === '/' ? 'index.html' : req.url); // Serve index.html or requested file
    const extname = String(path.extname(filePath)).toLowerCase();
    const mimeTypes = {
        '.html': 'text/html',
        '.js': 'text/javascript',
        '.css': 'text/css',
        '.json': 'application/json',
        '.png': 'image/png',
        '.jpg': 'image/jpg',
        '.gif': 'image/gif',
        '.svg': 'image/svg+xml',
        '.pdf': 'application/pdf',
        '.doc': 'application/msword',
        '.mp3': 'audio/mpeg',
        '.wav': 'audio/wav',
    };

    const contentType = mimeTypes[extname] || 'application/octet-stream';

    fs.readFile(filePath, (error, content) => {
        if (error) {
            if (error.code === 'ENOENT') {
                res.writeHead(404);
                res.end('404 Not Found');
            } else {
                res.writeHead(500);
                res.end('500 Internal Server Error: ' + error.code);
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content, 'utf-8');
        }
    });
};

// Create HTTPS server
const server = https.createServer(options, requestHandler);

// Set up WebSocket server
const wss = new WebSocket.Server({ server });

wss.on('connection', (ws) => {
    console.log('New connection made.');

    ws.on('message', (message) => {
        console.log('received: %s', message);
        // Broadcast the message to all clients
        wss.clients.forEach((client) => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(message);
            }
        });
    });

    ws.on('close', () => {
        console.log('Connection closed.');
    });
});

// Start the server
server.listen(port, '0.0.0.0', () => {
    console.log(`Server running at https://localhost:${port}/`);
});
