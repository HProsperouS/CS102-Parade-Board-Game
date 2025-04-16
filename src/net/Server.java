package net;

import app.*;
import app.board.*;
import app.user.Player;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utility.ConsoleController;

/**
 * Server contains the server-side code. It relies on {@link ClientHandler} to
 * communicate with each {@link Client}.
 * 
 * @author Amos
 */
public class Server {

    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;
    private MultiplayerController game;
    private List<Card> startingHand;

    /**
     * Port number the socket connection will be on. Each game will be using
     * a unique port number.
     */
    private static int port = Integer.parseInt(ParadeApp.properties.getProperty("PORT"));

    /**
     * Constructor for Server.
     * 
     * @param game Controller for the game. Used to trigger some methods in
     *             {@link MultiplayerController}.
     */
    public Server(MultiplayerController game) {
        while (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                // Unable to connect because port number is already in use and move to next port number
                port++;
            }
        }

        try {
            System.out.println("\nServer created on the following IP addresses:");

            // Get IP address of this machine and display it to console

            // Regular expression pattern for validating private IPv4 addresses
            Pattern pattern = Pattern.compile(
                    "(^10\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$)|" +
                            "(^172\\.1[6-9]{1}[0-9]{0,1}\\.[0-9]{1,3}\\.[0-9]{1,3}$)|" +
                            "(^172\\.2[0-9]{1}[0-9]{0,1}\\.[0-9]{1,3}\\.[0-9]{1,3}$)|" +
                            "(^172\\.3[0-1]{1}[0-9]{0,1}\\.[0-9]{1,3}\\.[0-9]{1,3}$)|" +
                            "(^192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}$)");

            Enumeration e = NetworkInterface.getNetworkInterfaces();
            int index = 1;
            while (e.hasMoreElements()) {
                // Get individual network interface
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                Enumeration ee = ni.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String ip = i.getHostAddress();
                    String name = ni.getDisplayName();

                    // Uses regex to ensure that it is an IPv4 address
                    Matcher matcher = pattern.matcher(ip);
                    boolean matchFound = matcher.find();

                    if (matchFound) {
                        if(index == 1){
                            System.out.println(
                                    "Your clients will only be able to conenct to one of them, based on their own network");
                        }
                        System.out.println(index + ") " + ip + " (" + name + ")");
                        index++;
                    }
                }
            }

            if(index == 1){
                System.out.println("Please ensure that you are connected to a network");
                System.out.println("If you are connected but there is nothing, the IP address from the router is not following private IP range");
            }

            System.out.println("\nServer created on port number: " + serverSocket.getLocalPort());
        } catch (SocketException e) {
            // Unknown IP address
            System.out.println("Error!");
        }

        this.game = game;
        this.startingHand = new ArrayList<>();
        clientHandlers = new ArrayList<>();
    }

    /**
     * Call this function to start the server and keep it running indefinitely.
     * 
     * @param numPlayers   Number of expected players.
     * @param startingHand {@link Player}'s starting hand.
     */
    public void startServer(int numPlayers, List<Card> startingHand) {
        System.out.println("Waiting for players...");
        int count = 0;
        this.startingHand = startingHand;

        try {

            // Keep the server up until expected number of players joined
            while (count < numPlayers) {
                Socket socket = serverSocket.accept();

                // Splits each cilent to a different thread
                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientHandlers.add(clientHandler);

                // The thread will invoke the run() method in clientHandler
                Thread thread = new Thread(clientHandler);
                thread.start();

                if (count < numPlayers - 1) {
                    this.broadcastMessage("waiting for more players to "
                            + "join...");
                }
                count++;
            }

            // First broadcast that all players have joined
            this.broadcastMessage("All players have joined!");

            // Add a delay after the message
            ConsoleController.consoleDelay(1000);

            // Then clear console for all clients
            this.clearAllClientsConsole();

        } catch (IOException e) {
            System.out.println("Error!");
            closeServerSocket();
        }
    }

    /**
     * Creates a new {@link Player} by calling {@link GameController#createPlayer}.
     * 
     * @param playerName Unqiue username of the {@link Player}.
     */
    public void createClientPlayer(String playerName) {
        game.createPlayer(playerName, startingHand);
    }

    /**
     * Sends a message to all {@link Client}s.
     * 
     * @param messageToSend Message to send.
     */
    public void broadcastMessage(String messageToSend) {
        System.out.println(messageToSend);

        // Iterate through all clientHanlders
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // Write to client's console
                clientHandler.write(clientHandler, messageToSend);
            } catch (IOException | NullPointerException e) {
                // If any client disconnects, disconnect all clients
                for (ClientHandler ch : clientHandlers) {
                    ch.closeEverything();
                }
                clientHandlers.clear();
                break;
            }
        }
    }

    /**
     * Sends a message to a specific {@link Client}.
     * 
     * @param username      Unique username of the {@link Client}.
     * @param messageToSend Message to send to the target {@link Client}.
     */
    public void sendMessage(String username, String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.getUsername().equals(username)) {
                    clientHandler.write(clientHandler, messageToSend);
                }
            } catch (IOException | NullPointerException e) {
                clientHandler.closeEverything();
            }
        }
    }

    /**
     * Returns the list of username of all {@link Client}s.
     * 
     * @return List containing all usernames.
     */
    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();

        for (ClientHandler clientHandler : clientHandlers) {
            usernames.add(clientHandler.getUsername());
        }

        return usernames;
    }

    /**
     * Returns the message from {@link Client} to be handled by
     * {@link GameController}. The message expected is a {@link Card} index.
     * 
     * @param message Message recieved from {@link Client}.
     * @return Card index entered by {@link Client}. Returns {@code -1} instead
     *         in the event of a disconnection message or an invalid input.
     */
    public int handleResponse(String message) {
        try {
            // Check if this is a disconnection message
            if (message == null || message.contains("DISCONNECT")) {
                System.out.println(message);
                return -1;
            }

            // Parse the card index
            int cardIndex = Integer.parseInt(message.trim());
            System.out.println("Card index received: " + cardIndex);
            return cardIndex;

        } catch (NumberFormatException e) {
            System.out.println("Error: Unable to parse index from message: "
                    + message);
            return -1;
        }
    }

    /**
     * Returns the last message sent by a specific {@link Client}.
     * 
     * @param username Unique username of the target {@link Client}.
     * @return The last integer message by {@link Client}. {@code} -1 is
     *         returned instead in the event that {@code username} has no
     *         match.
     */
    public int getClientMessage(String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUsername().equals(username)) {
                String message = clientHandler.getLastMessage();
                try {
                    return Integer.parseInt(message); // Convert to int
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number received from "
                            + username);
                }
            }
        }
        // invalid input
        return -1;
    }

    /**
     * Listens to a specific {@link Client}.
     * 
     * @param username Unique username of the target {@link Client}.
     * @return Index of the card chosen by the {@link Client}. Returns
     *         {@code -1} instead if the {@link Client}'s input is invalid.
     */
    public int listenFrom(String username) {
        // Default to -1 in case of error
        int cardChoice = -1;

        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.getUsername().equals(username)) {
                cardChoice = clientHandler.listen();
                break;
            }
        }

        if (cardChoice == -1) {
            System.out.println("No valid card choice received from "
                    + username);
        }

        return cardChoice;
    }

    /** Sends a clear console command to all clients. */
    public void clearAllClientsConsole() {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.write(clientHandler, "CLEAR_CONSOLE");
            } catch (IOException | NullPointerException e) {
                // If any client disconnects, disconnect all clients
                for (ClientHandler ch : clientHandlers) {
                    ch.closeEverything();
                }
                clientHandlers.clear();
                break;
            }
        }
    }

    /** Closes the server socket in case of an error */
    public void closeServerSocket() {
        try {
            for (ClientHandler clientHandler : clientHandlers) {
                clientHandler.closeEverything();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
