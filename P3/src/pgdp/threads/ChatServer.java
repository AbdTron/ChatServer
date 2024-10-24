package pgdp.threads;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;

public class ChatServer extends WebSocketServer {
    private SSLContext sslContext;

    public ChatServer(InetSocketAddress address) {
        super(address);
        try {
            // Load your keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream keyStoreFile = new FileInputStream("path/to/your/keystore.jks"); // Adjust the path
            keyStore.load(keyStoreFile, "keystorePassword".toCharArray()); // Use your keystore password

            // Set up key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "keyPassword".toCharArray()); // Use your key password

            // Set up trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // Set up SSL context
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("New connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from " + conn.getRemoteSocketAddress() + ": " + message);
        for (WebSocket client : getConnections()) {
            client.send(message); // Broadcast to all clients
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Chat server started on port: " + getPort());
    }

    protected SSLServerSocketFactory getSocketFactory() {
        return sslContext.getServerSocketFactory(); // Use the SSL context
    }

    public static void main(String[] args) {
        int port = 3003; // Default port
        if (args.length > 0) {
            port = Integer.parseInt(args[0]); // Override with argument if provided
        }
        ChatServer server = new ChatServer(new InetSocketAddress(port));
        server.start();
    }
}
