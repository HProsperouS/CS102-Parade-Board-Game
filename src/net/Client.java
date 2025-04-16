package net;

import app.ParadeApp;
import utility.ConsoleController;
import java.io.*;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Client contains the Client-side code. It connects to, listens from, and
 * sends data to {@link Server}.
 * 
 * @author Amos
 */
public class Client {

    private Socket socket;
    private BufferedReader in; // reads messages from server
    private BufferedWriter out; // writes messages to console
    private String username;
    private Scanner scanner = ParadeApp.scanner;
    private boolean isConnected = true; // Add a flag to track connection status

    /**
     * Constructor for Client. It initializes the reader and writer, as well as
     * creates a shutdown hook in the event that the user forces an exit.
     * 
     * @param socket Socket connection with {@link Server}.
     * 
     */
    public Client(Socket socket) {
        try {
            this.socket = socket;
            // messages sent by server is received by in
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Add shutdown hook to handle graceful disconnection on Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Disconnecting from server...");
                try {
                    // Only try to send disconnect message if socket is still connected
                    if (socket != null && !socket.isClosed() && socket.isConnected()) {
                        out.write(username + " DISCONNECTED\n");
                        out.flush();
                        socket.close();
                    }
                    System.out.println("Disconnected!");
                } catch (IOException e) {
                    // Ignore the error if the socket is already closed
                    if (!e.getMessage().equals("Stream closed")) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                }
            }));
        } catch (IOException e) {
            System.out.println("Error detected. Exiting application...");
            closeEverything(socket, in, out);
        }
    }

    /**
     * This method ensures that the username entered by the client
     * is unique.
     * 
     * @param username Username entered in by the client.
     * @return {@code true} if the username is valid; {@code false} otherwise.
     */
    public boolean attemptUserNameRegistration(String username) {
        try {
            this.username = username;
            // Send username to server in
            out.write(username);
            out.newLine();
            out.flush();

            String message = in.readLine();
            if (message == null) {
                closeEverything(socket, in, out);
            } else if (message.equals(username + " is taken!")) {
                this.username = null;
                return false;
            }
            return true;
        } catch (IOException e) {
            closeEverything(socket, in, out);
        }
        return false;
    }

    /**
     * Sends a message to {@link Server} (new Thread ensures that sending happens in
     * parallel to listening.
     */
    public void sendMessage() {
        try {
            while (socket.isConnected()) {
                // Check if there is another line to read before calling nextLine
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();

                    try {
                        // Ensure only an integer is sent
                        int number = Integer.parseInt(input);

                        // Send the integer to the server
                        out.write(String.valueOf(number));
                        out.newLine();
                        out.flush();

                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input! Please enter an integer.");
                    }
                } else {
                    // Handle case where input stream is unexpectedly closed or empty
                    System.out.println("No input available. Exiting.");
                    // Break the loop and exit gracefully
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("I/O error occurred: " + e.getMessage());
            e.printStackTrace();
            closeEverything(socket, in, out);

        } catch (NoSuchElementException e) {
            System.out.println("No more input available. Exiting.");
            e.printStackTrace();
            closeEverything(socket, in, out);
        } finally {
            // Ensure the scanner is closed to release resources
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Listens from {@link Server}. This method executes on a different thread.
     * 
     * @throws IOException Occurs when there is an unexpected user input.
     */
    public void listenForMessage() throws IOException {
        // Executes on a different thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;

                while (socket.isConnected() && isConnected) {
                    try {
                        message = in.readLine();
                        if (message == null) {
                            // Handle the null case first
                            System.out.println("\nServer has disconnected. Please press Ctrl+C to quit the game.");
                            isConnected = false;
                            closeEverything(socket, in, out);
                            break;
                        } else if (message.contains("Game ended")) {
                            System.out.println("\nGame has ended. Please press Ctrl+C to quit the game.");
                            isConnected = false;
                            closeEverything(socket, in, out);
                            break;
                        } else if (message.equals("CLEAR_CONSOLE")) {
                            ConsoleController.clearConsole();
                        } else {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        System.out.println("\nConnection to server lost. Please press Ctrl+C to quit the game.");
                        isConnected = false;
                        closeEverything(socket, in, out);
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * Closes the socket connection and other resources in the event of an
     * error.
     * 
     * @param socket Socket connection with {@link Server}.
     * @param in     BufferedReader to read messages from {@link Server}.
     * @param out    BufferedWriter to write messages to console.
     */
    public void closeEverything(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            isConnected = false;

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
     * Getter method for {@code username}.
     * 
     * @return The usernmae of this client.
     */
    public String getUsername() {
        return username;
    }
}