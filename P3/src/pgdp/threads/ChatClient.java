package pgdp.threads;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.time.LocalTime;
import java.util.Scanner;

public class ChatClient extends WebSocketClient {
    private String username;
    private String notificationHighlight = " ***** ";

    public ChatClient(URI serverUri, String username) {
        super(serverUri);
        this.username = username;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        display("Connected to server");
        send(username); // Send the username after connecting
    }

    @Override
    public void onMessage(String message) {
        display(message); // Handle received messages
        System.out.print("> ");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        display(notificationHighlight + " Connection closed: " + reason + notificationHighlight);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void display(String s) {
        System.out.println(LocalTime.now().toString() + ": " + s);
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int portNumber = 3003;
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the username: ");
        String userName = scan.nextLine();

        if (args.length == 2) {
            portNumber = Integer.parseInt(args[0]);
            serverAddress = args[1];
        }

        // Change the URI to use WSS (secure WebSocket)
        URI uri = URI.create("wss://" + serverAddress + ":" + portNumber);
        ChatClient client = new ChatClient(uri, userName);
        client.connect(); // Establish the connection

        System.out.println("\nHello.! Welcome to the chatroom.");
        System.out.println("Instructions:");
        System.out.println("1. Simply type the message to send broadcast to all active clients");
        System.out.println("2. Type '@username<space>yourmessage' without quotes to send message to desired client");
        System.out.println("3. Type 'WHOIS' without quotes to see list of active clients");
        System.out.println("4. Type 'LOGOUT' without quotes to logoff from server");
        System.out.println("5. Type 'PENGU' without quotes to request a random penguin fact");

        while (true) {
            System.out.print("> ");
            String msg = scan.nextLine();
            if (msg.equalsIgnoreCase("LOGOUT")) {
                client.send(new ChatMessage(ChatMessageType.LOGOUT, ""));  // Send logout message
                break;
            } else if (msg.equalsIgnoreCase("WHOIS")) {
                client.send(new ChatMessage(ChatMessageType.WHOIS, ""));  // Send WHOIS request
            } else if (msg.equalsIgnoreCase("PENGU")) {
                client.send(new ChatMessage(ChatMessageType.PINGUFACT, ""));  // Send request for a penguin fact
            } else {
                // Check if it's a direct message
                if (msg.startsWith("@")) {
                    client.send(new ChatMessage(ChatMessageType.MESSAGE, msg));  // Send direct message
                } else {
                    client.send(new ChatMessage(ChatMessageType.MESSAGE, msg));  // Send broadcast message
                }
            }
        }
        scan.close();
        client.close(); // Close the connection
    }
}
