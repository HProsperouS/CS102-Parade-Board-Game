package net;

import java.io.*;
import java.net.Socket;
import java.util.List;
import utility.ConsoleController;

/**
 * The ClientHandler is part of the {@link Server} application. Each
 * ClientHandler is responsible for handling the communication with a single
 * {@link Client} that has connected to the {@link Server}.
 * 
 * It listens for messages, processes requests from that particular
 * {@link Client}, and sends responses back.
 * 
 * ClientHandler enables broadcasting messages to all {@link Client}s connected
 * to the {@link Server}. It implements the Runnable interface, which allows
 * multi-threading. With multi-threading, each {@link Client} connection runs
 * in its own thread, allowing the {@link Server} to handle multiple client
 * messages simultaneously and broadcast messages to all clients.
 * 
 * @author Amos, Wei Bin
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in; // Reads client inputs
    private BufferedWriter out; // Sends data to client
    private String clientUsername;
    private Server server;
    private String lastMessage;

    /**
     * Constructor for ClientHandler.
     * 
     * @param socket Socket connection with the server.
     * @param server Holds the {@link Server} instance. It is used to trigger
     *               certain methods in {@link Server}.
     */
    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Server reads the first input by guest which is their username
            // loops if username is taken
            while (true) {
                clientUsername = in.readLine();

                // Check if user's username is the same as some other players
                List<String> usernames = server.getAllUsernames();
                boolean duplicateUsername = false;

                for (String name : usernames) {
                    // Ensure uniqueness of usernames for identification
                    if (name.equals(clientUsername)) {
                        write(this, clientUsername + " is taken!");
                        duplicateUsername = true;
                    }
                }

                // If no duplicate then break out of the loop
                if (!duplicateUsername) {
                    break;
                }
            }

            // Create new player & broadcast the username
            server.createClientPlayer(clientUsername);
            server.broadcastMessage(clientUsername + " joined the game!");

        } catch (IOException e) {
            closeEverything();
        }
    }

    /**
     * This method is called when a new Thread is created in {@link Server}.
     * This enables each ClientHandler object to run on it's own Thread, thereby
     * enabling sending and receiving messages between {@link Server} and each
     * {@link Client} simulateneously.
     */
    @Override
    public void run() {
    }

    /**
     * Listens to the socket connection.
     * 
     * @return The number entered by {@link Client}. In the event of any error,
     *         {@code -1} is returned instead.
     */
    public int listen() {
        try {
            if (!socket.isConnected() || socket.isClosed()) {
                System.out.println("Socket is closed or disconnected");
                return -1;
            }

            ConsoleController.clearInputBufferForPvP(in);
            lastMessage = in.readLine();

            if (lastMessage == null) {
                System.out.println("Client disconnected");
                closeEverything();
                return -1;
            }

            System.out.println("Message received from client: " + lastMessage);

            // Check if the message is not empty
            if (lastMessage.trim().isEmpty()) {
                System.out.println("Empty message received");
                return -1;
            }

            // Parse and return the card choice
            return server.handleResponse(lastMessage);

        } catch (IOException e) {
            System.out.println("Internal server error while reading from "
                    + "client.");
            closeEverything();
            return -1;
        }
    }

    /**
     * Writes to the specific client's terminal.
     * 
     * @param clientHandler Target client handler.
     * @param messageToSend Message to be sent to the target client.
     * @throws IOException Thrown in the event of an invalid input or output.
     */
    public void write(ClientHandler clientHandler, String messageToSend)
            throws IOException {
        clientHandler.out.write(messageToSend);
        clientHandler.out.newLine();
        clientHandler.out.flush();
    }

    /** Closes everything in the event of an error. */
    public void closeEverything() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter method for clientUsername.
     * 
     * @return String representing the username.
     */
    public String getUsername() {
        return clientUsername;
    }

    /**
     * Getter method for the last message received from {@link Server}.
     * 
     * @return The last message received.
     */
    public String getLastMessage() {
        return lastMessage;
    }

}
