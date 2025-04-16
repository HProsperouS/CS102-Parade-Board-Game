package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utility.Colour;
import utility.ConsoleController;

/**
 * ParadeApp is the main entry point of the application. It controls the console interface
 * and initializes the game based on user preferences. It works closely with {@link GameController},
 * {@link MultiplayerController}, and {@link SinglePlayerController} to manage different game modes.
 * 
 * The class handles:
 * 1. Loading game configurations from properties file
 * 2. Displaying welcome messages and game rules
 * 3. Managing user input for game setup
 * 4. Initializing appropriate game controllers based on user choices
 * 
 * @author Quanjun, Jia Jun
 */
public class ParadeApp {
    /** The username of the current player. */
    public static String name;
    /** The number of players in the game. */
    public static int numPlayers;
    /** Whether the game is in single-player mode. */
    public static boolean isSinglePlayer;
    /** Whether the game is using blackjack rules. */
    public static boolean isBlackjack;
    /** Scanner instance for reading user input. */
    public static Scanner scanner = new Scanner(System.in);
    /** Properties object for storing game configurations. */
    public static Properties properties = new Properties();

    /** Minimum number of players allowed in a game. */
    private static final int MIN_PLAYERS = 2;
    /** Maximum number of players allowed in a game. */
    private static final int MAX_PLAYERS = 6;

    /**
     * Main entry point of the application. Initializes the game and handles the main game flow.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // load game configurations
            loadProperties();

            // Display welcome message and menu
            displayWelcomeMessage();
            askForRules();
            startGameInstance();
        } catch (Exception e) {
            System.err.println("\nAn unexpected error occurred: "
                    + e.getMessage());
            System.err.println("Please try running the game again.");
            e.printStackTrace();
        } finally {
            scanner.close();
            System.out.println("\nThank you for playing Parade Card Game!");
        }
    }

    /**
     * Loads game configurations from the config.properties file.
     * 
     * @throws IOException If there is an error reading the properties file
     */
    public static void loadProperties() throws IOException {
        File f = new File("config.properties");
        String path = f.getAbsolutePath();
        properties.load(new InputStreamReader(
                new FileInputStream(path), StandardCharsets.UTF_8));
    }

    /**
     * Displays the welcome message and game rules to the user.
     * Includes ASCII art and basic game instructions.
     */
    private static void displayWelcomeMessage() {
        ConsoleController.clearConsole();
        System.out.println("=========================================================================================");
        System.out.println("██╗    ██╗███████╗██╗      ██████╗ ██████╗ ███╗   ███╗███████╗    ████████╗ ██████╗ ");
        System.out.println("██║    ██║██╔════╝██║     ██╔════╝██╔═══██╗████╗ ████║██╔════╝    ╚══██╔══╝██╔═══██╗");
        System.out.println("██║ █╗ ██║█████╗  ██║     ██║     ██║   ██║██╔████╔██║█████╗         ██║   ██║   ██║");
        System.out.println("██║███╗██║██╔══╝  ██║     ██║     ██║   ██║██║╚██╔╝██║██╔══╝         ██║   ██║   ██║");
        System.out.println("╚███╔███╔╝███████╗███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║███████╗       ██║   ╚██████╔╝");
        System.out.println(" ╚══╝╚══╝ ╚══════╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚══════╝       ╚═╝    ╚═════╝ ");
        System.out.println("             " + Colour.RED.apply("██████╗") + Colour.BLUE.apply("  █████╗")
                + Colour.ORANGE.apply(" ██████╗ ") + Colour.GREEN.apply(" █████╗ ") + Colour.PURPLE.apply("██████╗")
                + Colour.GREY.apply(" ███████╗"));
        System.out.println("             " + Colour.RED.apply("██╔══██╗") + Colour.BLUE.apply("██╔══██╗")
                + Colour.ORANGE.apply("██╔══██╗") + Colour.GREEN.apply("██╔══██╗") + Colour.PURPLE.apply("██╔══██╗")
                + Colour.GREY.apply("██╔════╝"));
        System.out.println("             " + Colour.RED.apply("██████╔╝") + Colour.BLUE.apply("███████║")
                + Colour.ORANGE.apply("██████╔╝") + Colour.GREEN.apply("███████║") + Colour.PURPLE.apply("██║  ██║")
                + Colour.GREY.apply("█████╗  "));
        System.out.println("             " + Colour.RED.apply("██╔═══╝") + Colour.BLUE.apply(" ██╔══██║")
                + Colour.ORANGE.apply("██╔══██╗") + Colour.GREEN.apply("██╔══██║") + Colour.PURPLE.apply("██║  ██║")
                + Colour.GREY.apply("██╔══╝  "));
        System.out.println("             " + Colour.RED.apply("██║     ") + Colour.BLUE.apply("██║  ██║")
                + Colour.ORANGE.apply("██║  ██║") + Colour.GREEN.apply("██║  ██║") + Colour.PURPLE.apply("██████╔╝")
                + Colour.GREY.apply("███████╗"));
        System.out.println("             " + Colour.RED.apply("╚═╝     ") + Colour.BLUE.apply("╚═╝  ╚═╝")
                + Colour.ORANGE.apply("╚═╝  ╚═╝") + Colour.GREEN.apply("╚═╝  ╚═╝") + Colour.PURPLE.apply("╚═════╝ ")
                + Colour.GREY.apply("╚══════╝"));
        System.out.println("=========================================================================================");
        System.out.println("\nGame Rules:");
        System.out.println("1. Each player starts with 5 cards");
        System.out.println("2. The parade line starts with 6 cards");
        System.out.println("3. On your turn, play a card from your hand");
        System.out.println("4. Cards in the parade line may be removed " +
                "based on:");
        System.out.println("   - The value of the played card");
        System.out.println("   - The color of the played card");
        System.out.println("5. The game ends when:");
        System.out.println("   - A player collects all 6 colors");
        System.out.println("   - The deck runs out and all players have 4 "
                + "or fewer cards");
        System.out.println("6. The player with the lowest score wins!");
        System.out.println("\nLet's begin!\n");
    }

    /**
     * Asks the user if they understand the game rules. If not, provides a URL
     * to the rulebook.
     */
    public static void askForRules() {

        // Checks if the user understands the rules
        String input = "";
        while (!(input.equals("yes") || input.equals("no"))) {

            System.out.print("\nDo you understand the rules? "
                    + "('Yes' or 'No'): ");

            if (scanner.hasNextLine()) {
                input = scanner.nextLine().toLowerCase();
            } else {
                ConsoleController.terminateConsoleOperation();
            }

            if (!(input.equals("yes") || input.equals("no"))) {
                System.out.println("Invalid input. Please enter either 'Yes' or 'No'!");
            }
        }

        if (input.equals("no")) {
            System.out.println("Here is the url to the rulebook");
            System.out.println("https://cdn.1j1ju.com/medias/8f/7e/"
                    + "8f-parade-rulebook.pdf\n");
        }

    }

    /**
     * Contains the main game flow logic. Handles:
     * 1. Starting a new game (single or multiplayer)
     * 2. Joining an existing game
     * 3. Initializing appropriate game controllers
     * 
     * @throws IOException If there are connection issues or player disconnections
     */
    private static void startGameInstance() {
        /*
         * Game initialization flow:
         * 1. Start new game
         *   1.1. Multiplayer
         *     1.1.1. Is blackjack? -> start server
         *   1.2. Single player
         *     1.2.1. Username -> start AI game
         * 2. Join game
         *   2.1. IP address
         *   2.2. Port
         *   2.3. Username
         */
        try {
            boolean startGame = askForStartOrJoin();

            if (startGame) {
                // Determine if user wants to play against AI or other players
                isSinglePlayer = askForSinglePlayer();

                if (!isSinglePlayer) {
                    // print the Blackjack rules before asking if they wanna play blackjack
                    displayBlackjackRules();
                    
                    // Create multiplayer game with server
                    GameController gc = new MultiplayerController(startGame);
                    gc.startGame();
                } else {
                    // Create an instance of gameController of P V AI
                    GameController gc = new SinglePlayerController();
                    gc.startGame();
                }

            } else {
                // Join existing multiplayer game
                new MultiplayerController(startGame);
            }

        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Player disconnected")) {
                System.out.println("\nA player has disconnected from the game.");
                System.out.println("Game ended. Returning to main menu...");
            } else if (e.getMessage() != null && e.getMessage().contains("Game has ended")) {
                System.out.println("Game ended. Returning to main menu...");
            } else {
                System.out.println("\nConnection error occurred: " + e.getMessage());
                System.out.println("Returning to main menu...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nAn unexpected error occurred: " + e.getMessage());
            System.out.println("Returning to main menu...");
        }
    }

    /**
     * Displays the rules for BlackJack mode to the user.
     */
    private static void displayBlackjackRules() {
        System.out.println("\n" + Colour.GREEN.apply("BlackJack Mode Rules:"));
        System.out.println(Colour.GREEN.apply("1. All original Parade game rules remain unchanged"));
        System.out.println(Colour.GREEN.apply("2. Each player starts with $1000"));
        System.out.println(Colour.GREEN.apply("3. Players place wagers on their collected cards for each Parade round"));
        System.out.println(Colour.GREEN.apply("4. Goal: Collect cards summing closest to 15 without going over"));
        System.out.println(Colour.GREEN.apply("5. Winners receive their wagered amount; losers lose their bet"));
        System.out.println(Colour.GREEN.apply("6. Parade game winner gets an $800 bonus"));
        System.out.println(Colour.GREEN.apply("7. Player with the highest bankroll at the end wins"));
    }

    /**
     * Prompts the user to enter the number of human players for the game.
     * 
     * @return The number of human players (between {@link #MIN_PLAYERS} and {@link #MAX_PLAYERS})
     */
    public static int getNumberOfPlayers() {
        int numPlayers;
        String input;

        while (true) {
            System.out.print("\nEnter number of human players (" + MIN_PLAYERS + "-" + MAX_PLAYERS + "): ");

            try {
                // Check if input is available
                if (scanner.hasNextLine()) {
                    input = scanner.nextLine().trim();
                    
                    // Check for empty input
                    if (input.isEmpty()) {
                        System.out.println("Please enter a number. Input cannot be empty!");
                        continue;
                    }
                    
                    numPlayers = Integer.parseInt(input);

                    // Check if the input is within the valid range
                    if (numPlayers >= MIN_PLAYERS && numPlayers <= MAX_PLAYERS) {
                        return numPlayers; // Valid input, exit loop
                    } else {
                        System.out.println("Invalid input. Number of human players must be between "
                                + MIN_PLAYERS + " and " + MAX_PLAYERS);
                    }
                } else {
                    // Handle case where user presses Ctrl + C
                    ConsoleController.terminateConsoleOperation();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number!");
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again!");
                // Clear the buffer to avoid infinite loop
                scanner.nextLine(); 
            }
        }
    }

    /**
     * Prompts the user to enter the number of AI players for the game.
     * 
     * @param minimum Minimum number of AI players required
     * @param numberOfHumanPlayers Number of human players already in the game
     * @return The number of AI players to add
     */
    public static int getNumberOfAIs(int minimum, int numberOfHumanPlayers) {
        int numPlayers;
        String input;

        while (true) {
            System.out.print(
                    "\nEnter number of AI players (" + minimum + "-" + (MAX_PLAYERS - numberOfHumanPlayers) + "): ");

            try {
                // Check if input is available
                if (scanner.hasNextLine()) {
                    input = scanner.nextLine().trim();
                    
                    // Check for empty input
                    if (input.isEmpty()) {
                        System.out.println("Please enter a number. Input cannot be empty!");
                        continue;
                    }
                    
                    numPlayers = Integer.parseInt(input);

                    // Check if the input is within the valid range
                    if (numPlayers >= minimum && numPlayers <= (MAX_PLAYERS - numberOfHumanPlayers)) {
                        // Valid input, exit loop
                        return numPlayers; 
                    } else {
                        System.out.println("Invalid input. Number of AI players must be between "
                                + minimum + " and " + (MAX_PLAYERS - numberOfHumanPlayers));
                    }
                } else {
                    // Handle case where user presses Ctrl + C
                    ConsoleController.terminateConsoleOperation();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number!");
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again!");
                // Clear the buffer to avoid infinite loop
                scanner.nextLine(); 
            }
        }
    }

    /**
     * Prompts the user to enter their username.
     * 
     * @return The validated username entered by the user
     */
    public static String getUserOwnName() {
        String name;
        // Prompt user for their username until a valid one is provided
        while (true) {
            System.out.print("\nEnter your username: ");

            try {
                if (scanner.hasNextLine()) {
                    name = scanner.nextLine().trim();

                    if (!name.isEmpty()) {
                        return name;
                    } else {
                        System.out.println("Invalid input. Please enter a valid name.");
                    }
                } else {
                    // Handle Ctrl + C interruption by terminating console operation
                    ConsoleController.terminateConsoleOperation();
                }
            } catch (Exception e) {
                System.out.println("An error occurred. Please enter a valid username.");
                // Clear invalid input from scanner buffer
                scanner.nextLine();
            }
        }
    }

    /**
     * Prompts the user to choose between single-player and multiplayer mode.
     * 
     * @return {@code true} if single-player mode is selected, {@code false} for multiplayer
     */
    private static boolean askForSinglePlayer() {
        boolean isSinglePlayer = false;

        try {
            String response;
            do {
                System.out.print("\nDo you want to play single player? ('Yes' or 'No'): ");

                // Check if input is available
                if (!scanner.hasNextLine()) {
                    ConsoleController.terminateConsoleOperation();
                }

                response = scanner.nextLine().trim().toLowerCase();

                if (!response.equals("yes") && !response.equals("no")) {
                    System.out.println("Invalid input. Please enter 'Yes' or 'No'!");
                }
            } while (!response.equals("yes") && !response.equals("no"));

            isSinglePlayer = response.equals("yes");

        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

        return isSinglePlayer;
    }

    /**
     * Prompts the user to choose between starting a new game or joining an existing one.
     * 
     * @return {@code true} if starting a new game, {@code false} if joining an existing game
     */
    private static boolean askForStartOrJoin() {

        String actionToServer = "";
        while (!(actionToServer.equals("start") || actionToServer.equals("join"))) {
            System.out.print("\nDo you want to start a new game or join"
                    + " a server? ('Start' or 'Join'): ");
            actionToServer = scanner.nextLine().trim().toLowerCase();

            if (!(actionToServer.equals("start") || actionToServer.equals("join"))) {
                System.out.println("Invalid input. Please enter either 'Start' "
                        + "or 'Join'! ");
            }
        }

        return actionToServer.equals("start");
    }

    /**
     * Prompts the user to enter the server's IP address when joining a game.
     * 
     * @return The validated IP address or "localhost"
     */
    public static String getIPAddress() {
        String ip = "";
        boolean matchFound = false;
        
        // Regular expression pattern for validating IPv4 addresses
        Pattern pattern = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");

        while (!(matchFound || ip.equals("localhost"))) {
            System.out.print("\nPlease enter the server's ip address (e.g. '192.168.196.180' or 'localhost'): ");
            
            try {
                // Check if input is available
                if (scanner.hasNextLine()) {
                    ip = scanner.nextLine().trim().toLowerCase();

                    // checks input with regex
                    Matcher matcher = pattern.matcher(ip);
                    matchFound = matcher.find();

                    if (!(matchFound || ip.equals("localhost"))) {
                        System.out.println(
                                "Invalid input. Please enter a valid ip address (e.g. '192.168.1.1' or 'localhost')! ");
                    }
                } else {
                    // Handle case where user presses Ctrl + C
                    ConsoleController.terminateConsoleOperation();
                }
            } catch (Exception e) {
                // Handle any other exceptions
                ConsoleController.terminateConsoleOperation();
            }
        }
        return ip;
    }

    /**
     * Prompts the user to enter the server's port number when joining a game.
     * 
     * @return The validated port number (0-65535)
     */
    public static int getPortNumber() {
        int port = -1;
        int defaultPort = Integer.parseInt(ParadeApp.properties.getProperty("PORT"));

        while (!(port >= 0 && port <= 65535)) {
            // Port numbers must be between 0 and 65535 (inclusive)
            System.out.print("\nPlease enter the desired port number (0-65535, default is " + defaultPort + "): ");

            try {
                // Check if input is available
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    
                    // Check for empty input
                    if (input.isEmpty()) {
                        System.out.println("Please enter a number. Input cannot be empty!");
                        continue;
                    }
                    
                    port = Integer.parseInt(input);

                    // Check if the input is within the valid range
                    if (!(port >= 0 && port <= 65535)) {
                        System.out.println("Invalid port number. Please enter a number between 0 and 65535!");
                        // Reset port to continue loop
                        port = -1; 
                    }
                } else {
                    // Handle Ctrl + C interruption by terminating console operation
                    ConsoleController.terminateConsoleOperation();
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number!");
                // Reset port to continue loop
                port = -1; 
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again!");
                // Reset port to continue loop
                port = -1; 
                // Clear invalid input to prevent infinite loop
                scanner.nextLine();
            }
        }
        return port;
    }

    /**
     * Prompts the user to choose whether to play with blackjack rules.
     * 
     * @return {@code true} if blackjack rules are enabled, {@code false} otherwise
     */
    public static boolean askForBlackjack() {
        boolean isBlackjack = false;

        try {
            String response;
            do {
                System.out.print("\nDo you want to play with blackjack rules? ('Yes' or 'No'): ");

                // Check if input is available
                if (!scanner.hasNextLine()) {
                    ConsoleController.terminateConsoleOperation();
                }

                response = scanner.nextLine().trim().toLowerCase();

                if (!response.equals("yes") && !response.equals("no")) {
                    System.out.println("Invalid input. Please enter 'Yes' or 'No'!");
                }
            } while (!response.equals("yes") && !response.equals("no"));

            isBlackjack = response.equals("yes");

        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

        return isBlackjack;
    }

}