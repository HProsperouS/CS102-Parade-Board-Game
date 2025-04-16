package app;

import app.board.*;
import app.user.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;
import net.*;
import utility.*;

/**
 * MultiplayerController contains the game flow and game logic for a
 * multiplayer game. A multiplayer game is defined as a game that involves
 * more than 1 {@link HumanPlayer}s and 0 or more {@link ComputerPlayer}s. A
 * {@link Server}-{@link Client} connection is required for each
 * {@link HumanPlayer}.
 * 
 * @author Amos
 */
public class MultiplayerController extends GameController {

    /** Server instance for managing multiplayer connections. */
    private Server server;

    /** Flag indicating if blackjack mode is enabled. */
    private boolean isBlackjack;

    /** Blackjack game instance if mode is enabled. */
    private BlackJack blackjack;

    /**
     * Constructor for playing in multiplayer mode. In this case,
     * there can be a mix of {@link HumanPlayer}s and {@link ComputerPlayer}s,
     * and there can be 0 {@link ComputerPlayer}s as well. A
     * {@link Server}-{@link Client} connection is required.
     * 
     * @param createServer If {@code true}, then start a {@link Server}.
     *                     Otherwise, connect to an existing {@link Server}
     *                     instead.
     * @throws IOException May be thrown when {@link Client} listens for a message.
     *                     See {@link Client#listenForMessage}.
     */
    public MultiplayerController(boolean createServer) throws IOException {
        // If create server, create a server & initialise game object
        if (createServer) {
            this.deck = new Deck();
            this.players = new ArrayList<>();
            this.scoreBoard = new HashMap<>();
            this.isBlackjack = ParadeApp.askForBlackjack();

            int numberOfPlayers = ParadeApp.getNumberOfPlayers();

            // AI players can't play blackjack, only real players
            int numberOfAI = 0;
            if (!isBlackjack) {
                numberOfAI = ParadeApp.getNumberOfAIs(0, numberOfPlayers);
                System.out.println("Number of AI players: " + numberOfAI);
            }

            // Initialize parade line
            List<Card> initialParadeCards = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                initialParadeCards.add(deck.drawCard());
            }
            paradeLine = new ParadeLine(initialParadeCards);

            // Pass remaining card to server which will distribute the cards
            // cards allocated for all the player's initial hand
            List<Card> clientsDeck = new ArrayList<>();
            int deckQuantity = (numberOfPlayers + numberOfAI) * 5;
            for (int i = 0; i < deckQuantity; i++) {
                clientsDeck.add(deck.drawCard());
            }
            System.out.println("Initial number of cards in clientsDeck: "
                    + clientsDeck.size());
            server = new Server(this);

            // Pass clientDeck to server which will handle the passing
            server.startServer(numberOfPlayers, clientsDeck);

            for (int i = 0; i < numberOfAI; i++) {
                createPlayer(clientsDeck);
            }

            if (isBlackjack) {
                // Generate a list of HumanPlayers
                List<HumanPlayer> humanPlayers = players.stream()
                        .map(a -> (HumanPlayer) a)
                        .collect(Collectors.toCollection(ArrayList::new));
                this.blackjack = new BlackJack(humanPlayers, server);
            }
        } else {
            // Create a client object
            Socket socket = null;
            String ip = "";
            int port = -1;

            // Loop to check if can establish server connection
            // as it will fail if the server is not on the same ip and port
            while (true) {
                try {
                    ip = ParadeApp.getIPAddress();
                    port = ParadeApp.getPortNumber();
                    socket = new Socket(ip, port);
                    break;

                } catch (Exception e) {
                    System.out.println("Server with the address " + ip + ":" + port + " does not exist");
                }
            }

            Client client = new Client(socket);

            // Validate username
            while (true) {
                String username = ParadeApp.getUserOwnName();
                boolean usernameStatus = client.attemptUserNameRegistration(
                        username);

                // Ensure uniqueness of username
                if (usernameStatus) {
                    System.out.println("You have joined the server!");
                    break;
                } else {
                    System.out.println(username + " is taken!");
                }
            }

            client.listenForMessage();
            client.sendMessage();
        }
    }

    /**
     * Starts a game played between {@link HumanPlayer}s (and optionally
     * {@link ComputerPlayer}s). This method contains the game loop.
     * 
     * @throws IOException Occurs when there is error in handling a turn when
     *                     playing against other {@link HumanPlayer}s. This
     *                     Exception is thrown when there is an invalid input.
     */
    @Override
    public void startGame() throws IOException {
        // Initialize the variables needed for the game
        initializeGame(server::broadcastMessage);

        // Used only for blackjack mode
        Map<HumanPlayer, List<Card>> collectedPerPlayer = new HashMap<>();

        // Display game processing message
        ConsoleController.consoleDelay(1500);
        server.broadcastMessage(AsciiArt.gameProcessing());

        // Add a delay before clearing the console
        ConsoleController.consoleDelay(1000);

        // Display BlackJack banner if in BlackJack mode. After "game is starting..."
        if (isBlackjack) {
            server.broadcastMessage(AsciiArt.getBlackJackBanner());
        }

        // Main game loop
        while (!gameEnded) {

            // collect wagers if playing in blackjack mode
            if (isBlackjack && playerTurn == 0) {
                blackjack.collectWagers();
            }

            // Clear console for all clients at the start of each turn
            server.clearAllClientsConsole();

            // Retrieve the current player's name
            String currentPlayerName = players.get(playerTurn).getName();

            // Display turn info
            displayTurnInfo(server::broadcastMessage, currentPlayerName);

            // Print out the current parade line
            displayCurrentParadeLine(server::broadcastMessage);

            // Show all player's collected cards
            displayAllCollectedCards(server::broadcastMessage);

            // Get player at current round
            Player currentPlayer = players.get(playerTurn);
            List<Card> collectedCards = handlePlayerTurn(currentPlayer);

            // Handle blackjack logic
            if (isBlackjack && (playerTurn + 1) % players.size() == 0) {
                collectedPerPlayer.put((HumanPlayer) currentPlayer,
                        collectedCards);
                blackjack.getRoundWinner(collectedPerPlayer);
                collectedPerPlayer.clear();
            } else if (isBlackjack) {
                collectedPerPlayer.put((HumanPlayer) currentPlayer,
                        collectedCards);
            }

            /*
             * Check if game has ended, game may end after the previous
             * person's turn --> check condition again
             */
            gameEnded = isGameEnded();

            if (!gameEnded) {
                // Move to next player
                playerTurn = (playerTurn + 1) % players.size();

                // If it passes back to first player, means next turn
                if (playerTurn == 0) {
                    turnNumber++;
                }
            }

            // When going to final round, should start from first player again
            if (finalRound && !skipToFinal) {
                displayFinalRoundAscii(server::broadcastMessage);
            }

        }

        // Clear console for all clients
        server.clearAllClientsConsole();

        // Display all players' collected cards before discarding happens
        displayAllCollectedCards(server::broadcastMessage);

        // Discard 2 cards
        discardTwoCards(server::broadcastMessage);

        // Display final collected cards
        server.clearAllClientsConsole();
        displayAllCollectedCards(server::broadcastMessage);

        // Display score of each player
        displayScores(server::broadcastMessage);
        List<Player> paradeWinner = getWinner();
        String winnerNames = paradeWinner.stream()
                .map(a -> a.getName())
                .collect(Collectors.joining(", "));

        // Display winner
        displayWinner(paradeWinner, winnerNames, server::broadcastMessage);

        // For blackjack mode, calculate overall winnings
        if (isBlackjack) {
            server.broadcastMessage(winnerNames
                    + " wins an extra 800 for winning parade!");
            paradeWinner.stream()
                    .map(a -> (HumanPlayer) a)
                    .forEach(a -> a.modifyBankroll(800));
            double max = 0;
            HumanPlayer winner = null;
            for (Player p : players) {
                if (p instanceof HumanPlayer hp) {
                    server.broadcastMessage(hp.getName()
                            + " has ended with the amount of money "
                            + hp.getBankroll());
                    if (max < hp.getBankroll()) {
                        max = hp.getBankroll();
                        winner = hp;
                    }
                }
            }

            server.broadcastMessage("Congratulations " + winner.getName()
                    + " for winning blackjack parade with "
                    + winner.getBankroll());
        }
        server.broadcastMessage("Game ended. Click Ctrl + C to exit!");

        server.closeServerSocket();
        scanner.close();
    }

    /**
     * Handles a {@link Player}'s turn. This method is used for a multiplayer
     * game.
     * 
     * @param player Current {@link HumanPlayer} playing.
     * @return A list of {@link Card}s that is collected at the end of the
     *         turn.
     * @throws IOException This Exception is thrown when a {@link HumanPlayer}
     *                     disconnects from the {@link Server}.
     */
    @Override
    public List<Card> handlePlayerTurn(Player player) throws IOException {
        List<Card> playerHand = player.getHand();
        ConsoleController.consoleDelay(500);
        Card cardToPlay;

        if (player instanceof HumanPlayer hPlayer) {
            String playerUsername = hPlayer.getName();

            server.broadcastMessage("" + playerUsername + "'s turn.");
            ConsoleController.consoleDelay(300);
            server.sendMessage(playerUsername, "Your card has been added to your hand!");
            server.sendMessage(playerUsername, "Your hand: ");
            server.sendMessage(playerUsername, hPlayer.displayHand());
            server.sendMessage(playerUsername, "Choose a card to play (1-"
                    + playerHand.size() + "): ");

            // Listen for player
            while (true) {
                try {
                    int playerCardChoice = server.listenFrom(playerUsername);
                    if (playerCardChoice == -1) {
                        // Player disconnected
                        server.broadcastMessage(playerUsername
                                + " has disconnected. Game ends!");
                        server.broadcastMessage("All players, "
                                + "press CTRL + C to exit!");
                        throw new IOException("Player disconnected");
                    }

                    // Player will send a card
                    int cardIndex = playerCardChoice - 1;
                    // Play the card
                    cardToPlay = playerHand.get(cardIndex);
                    server.broadcastMessage("" + playerUsername + " has chosen:");
                    List<Card> cards = List.of(cardToPlay);
                    server.broadcastMessage(AsciiArt.printLine(cards));
                    // Discard card from hand
                    hPlayer.removeCardFromHand(cardIndex);
                    break;
                } catch (IndexOutOfBoundsException e) {
                    server.sendMessage(playerUsername,
                            "Please input a value between 1 - 5");
                }
            }

        } else {
            ConsoleController.consoleDelay(1500);
            cardToPlay = ((ComputerPlayer) player).playCard(paradeLine);
            server.broadcastMessage(player.getName() + " has chosen:");
            server.broadcastMessage(AsciiArt.printLine(List.of(cardToPlay)));
            ConsoleController.consoleDelay(2000);
        }

        // Add played card to parade line & collect card from paradeLine
        List<Card> drawnCards = paradeLine.collectCardFromParade(cardToPlay);

        player.addToCollection(drawnCards);

        // Draw new card for player only if not in final round
        if (!finalRound && !deck.isDeckEmpty()) {
            player.getCardFromDeck(deck.drawCard());
        }

        return drawnCards;
    }

    /**
     * Asks the {@link HumanPlayer}s to discard 2 {@link Card}s from their
     * hands.
     * 
     * @param player Target {@link HumanPlayer}.
     * @return The 2 indicies of the {@link Card}s to discard.
     */
    @Override
    public int[] chooseCardsToDiscard(HumanPlayer player) {
        // Send the player's own collected cards privately
        String playerUsername = player.getName();
        server.sendMessage(playerUsername, "Your collected cards:");
        server.sendMessage(playerUsername, player.displayCollected());
        ConsoleController.consoleDelay(300);

        // Send the player's hand privately
        server.sendMessage(playerUsername, "Your hand:");
        server.sendMessage(playerUsername, player.displayHand());

        List<Card> playerCurrentHand = player.getHand();
        int firstCardIndex = -1;
        int secondCardIndex = -1;

        while (true) {
            try {
                server.sendMessage(playerUsername,
                        "Choose the 1st card to discard (1-"
                                + playerCurrentHand.size() + "): ");
                // Listen for player
                firstCardIndex = server.listenFrom(playerUsername) - 1;

                if (firstCardIndex >= 0
                        && firstCardIndex < playerCurrentHand.size()) {
                    ConsoleController.consoleDelay(200);
                    System.out.println(AsciiArt.gameLoading());
                    break;
                } else {
                    server.sendMessage(playerUsername,
                            "Invalid choice! Please enter a number between "
                                    + "1 and " + playerCurrentHand.size());
                }
            } catch (Exception e) {
                // Clear invalid input
                scanner.nextLine();
            }
        }

        // Ask for second card
        while (true) {
            try {
                server.sendMessage(playerUsername,
                        "Choose the 2nd card to discard (1-"
                                + playerCurrentHand.size() + "): ");
                secondCardIndex = server.listenFrom(playerUsername) - 1;

                if (secondCardIndex >= 0
                        && secondCardIndex < playerCurrentHand.size()
                        && firstCardIndex != secondCardIndex) {
                    ConsoleController.consoleDelay(200);
                    System.out.println(AsciiArt.gameLoading());
                    break;
                } else if (firstCardIndex == secondCardIndex) {
                    server.sendMessage(playerUsername,
                            "Invalid choice! Please choose a different card.");
                } else {
                    server.sendMessage(playerUsername,
                            "Invalid choice! Please enter a number between "
                                    + "1 and " + playerCurrentHand.size());
                }
            } catch (Exception e) {
                // Clear invalid input
                scanner.nextLine();
            }
        }

        return new int[] { firstCardIndex, secondCardIndex };
    }

    /**
     * Prints the message received from {@link Client} to the {@link Server}
     * terminal.
     * 
     * @param message Message received from {@link Client}.
     * @return Message received from {@link Client}.
     */
    public int handleMessageFromClients(int message) {
        System.out.println("player chose card number " + message);
        return message;
    }

}
