package app;

import java.util.*;
import java.util.stream.Collectors;

import app.board.*;
import app.user.*;
import net.*;
import utility.*;

/**
 * MultiplayerController contains the game flow and game logic for a
 * multiplayer game. A multiplayer game is defined as a game that involves
 * exactly 1 {@link HumanPlayer} and 1 or more {@link ComputerPlayer}s. A
 * {@link Server}-{@link Client} connection is not required.
 * 
 * @author Amos
 */
public class SinglePlayerController extends GameController {

    /**
     * Constructor for playing in single-player mode. In this
     * case, we are expecting only 1 {@link HumanPlayer}. The rest of the
     * players are {@link ComputerPlayer}s. A {@link Server}-{@link Client}
     * connection is not required in this case.
     */
    public SinglePlayerController() {
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.scoreBoard = new HashMap<>();

        // Get game details from user
        String username = ParadeApp.getUserOwnName();
        int numberOfPlayers = ParadeApp.getNumberOfAIs(1, 1);

        // Initialize parade line with 6 cards
        List<Card> initialParadeCards = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            initialParadeCards.add(deck.drawCard());
        }
        paradeLine = new ParadeLine(initialParadeCards);

        // Create initial hand for human player
        List<Card> initialHand = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            initialHand.add(this.deck.drawCard());
        }
        // Add human player to game
        players.add(new HumanPlayer(username, initialHand));

        // Initialize AI players with their hands
        for (int i = 0; i < numberOfPlayers; i++) {
            List<Card> initialComputerHand = new ArrayList<Card>();
            for (int j = 0; j < 5; j++) {
                initialComputerHand.add(this.deck.drawCard());
            }
            players.add(new ComputerPlayer(initialComputerHand));
        }
        // Clear console before game starts
        ConsoleController.clearConsole();
    }

    /**
     * Starts a game played between a {@link HumanPlayer} and
     * {@link ComputerPlayer}s. This method contains the game loop.
     */
    @Override
    public void startGame() {
        // Initialize game state
        initializeGame(System.out::println);

        // Main game loop
        while (!gameEnded) {
            ConsoleController.clearConsole();

            // Get current player's name
            String currentPlayerName = players.get(playerTurn).getName();

            // Display turn info
            displayTurnInfo(System.out::println, currentPlayerName);

            // Print out the current parade line
            displayCurrentParadeLine(System.out::println);

            // Show all player's collected cards
            displayAllCollectedCards(System.out::println);

            // Process current player's turn
            Player currentPlayer = players.get(playerTurn);
            handlePlayerTurn(currentPlayer);

            // Check if game has ended
            gameEnded = isGameEnded();

            if (!gameEnded) {
                // Move to next player
                playerTurn = (playerTurn + 1) % players.size();

                // Increment turn number if back to first player
                if (playerTurn == 0) {
                    turnNumber++;
                }
            }

            // Handle final round transition
            if (finalRound && !skipToFinal) {
                displayAllCollectedCards(System.out::println);
            }
        }

        // Game end sequence
        ConsoleController.clearConsole();
        discardTwoCards(System.out::println);
        ConsoleController.clearConsole();
        displayAllCollectedCards(System.out::println);
        displayScores(System.out::println);
        
        // Determine and display winner
        List<Player> paradeWinner = getWinner();
        String winnerNames = paradeWinner.stream()
                .map(a -> a.getName())
                .collect(Collectors.joining(", "));
        displayWinner(paradeWinner, winnerNames, System.out::println);
        scanner.close();
    }

    /**
     * Handles a {@link Player}'s turn. This method is used for a single
     * player game.
     * 
     * @param player Instance of a player.
     * @return A list of {@link Card}s that is collected at the end of the
     *         turn.
     */
    @Override
    public List<Card> handlePlayerTurn(Player player) {
        List<Card> playerCurrentHand = player.getHand();
        ConsoleController.consoleDelay(500);

        // Display game state for human player
        if (player instanceof HumanPlayer hPlayer) {
            ConsoleController.consoleDelay(300);
            System.out.println("Your card has been added to your hand!");
            System.out.println("Your hand: ");
            System.out.println(hPlayer.displayHand());
        }
        ConsoleController.consoleDelay(1500);

        // Get card to play from player
        Card playedCard;
        while (true) {
            try {
                playedCard = playCard(player);
                break;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Pick a Valid range of 1 to "
                        + playerCurrentHand.size());
            } catch (NumberFormatException e) {
                System.out.println("Input an actual number");
            } catch (IndexOutOfBoundsException | InputMismatchException e) {
                System.out.println("Pick a Valid range of 1 to "
                        + playerCurrentHand.size());
            }
        }

        // Process card play
        List<Card> drawnCards = paradeLine.collectCardFromParade(playedCard);
        player.addToCollection(drawnCards);

        // Draw new card if not in final round
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
        // Display other players' collected cards
        for (Player p : players) {
            if (!p.equals(player)) {
                System.out.println(p.getName() + "'s collected card is");
                System.out.println(p.displayCollected());
            }
        }

        // Display current player's cards
        System.out.println("Your collected cards");
        System.out.println(player.displayCollected());
        ConsoleController.consoleDelay(300);
        System.out.println("Your hand: ");
        System.out.println(player.displayHand());

        List<Card> playerCurrentHand = player.getHand();
        int firstCardIndex = -1;
        int secondCardIndex = -1;

        // Get first card to discard
        while (true) {
            System.out.print("Choose the 1st card to discard (1-"
                    + playerCurrentHand.size() + "): ");
            if (scanner.hasNextInt()) {
                firstCardIndex = scanner.nextInt() - 1;
                scanner.nextLine(); 
            } else {
                scanner.next(); 
                continue;
            }

            if (firstCardIndex >= 0
                    && firstCardIndex < playerCurrentHand.size()) {
                ConsoleController.consoleDelay(200);
                System.out.println(AsciiArt.gameLoading());
                break;
            } else {
                System.out.println("Invalid choice! Please enter a number"
                        + " between 1 and " + playerCurrentHand.size());
            }
        }

        // Get second card to discard
        while (true) {
            System.out.print("Choose the 2nd card to discard (1-" + playerCurrentHand.size() + "): ");
            if (scanner.hasNextInt()) {
                secondCardIndex = scanner.nextInt() - 1;
                scanner.nextLine(); 
            } else {
                scanner.next(); 
                continue;
            }

            if (secondCardIndex >= 0
                    && secondCardIndex < playerCurrentHand.size()
                    && firstCardIndex != secondCardIndex) {
                ConsoleController.consoleDelay(200);
                System.out.println(AsciiArt.gameLoading());
                break;
            } else if (firstCardIndex == secondCardIndex) {
                System.out.println("Invalid choice! Please choose a "
                        + " different card.");
            } else {
                System.out.println(
                        "Invalid choice! Please enter a number between 1 and "
                                + playerCurrentHand.size());
            }
        }

        return new int[] { firstCardIndex, secondCardIndex };
    }
}
