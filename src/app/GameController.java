package app;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import app.board.*;
import app.user.*;
import net.*;
import utility.*;

/**
 * Controls the game flow and contains core game logic for the Parade card game.
 * This abstract class serves as the base for both single and multiplayer game modes.
 * 
 * The class manages:
 * 1. Game state and turn management
 * 2. Player interactions and card plays
 * 3. Score calculation and winner determination
 * 4. Final round handling
 * 
 * @author Quanjun, Jia Jun
 */
public abstract class GameController {
    
    /** Scanner for user input. */
    protected Scanner scanner = ParadeApp.scanner;
    
    /** Deck of cards for the game. */
    protected Deck deck;
    
    /** Current parade line of cards. */
    protected ParadeLine paradeLine;
    
    /** List of players in the game. */
    protected List<Player> players;
    
    /** Score tracking for each player. */
    protected Map<Player, Integer> scoreBoard;
    
    /** Flag indicating if game is in final round. */
    protected boolean finalRound = false;
    
    /** Current player's turn index. */
    protected int playerTurn;
    
    /** Current turn number. */
    protected int turnNumber;
    
    /** Flag indicating if game has ended. */
    protected boolean gameEnded;
    
    /** Flag to skip to final round. */
    protected boolean skipToFinal;

    /**
     * Creates a new {@link HumanPlayer} to the game.
     * 
     * @param playerName  Unique username of the {@link HumanPlayer}.
     * @param clientsDeck Contains a subset of {@link Deck} that will be used
     *                    to create the {@link Player}'s initial hand.
     */
    public void createPlayer(String playerName, List<Card> clientsDeck) {
        System.out.println("number of cards before creating player: "
                + clientsDeck.size());
        List<Card> playerHand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            playerHand.add(clientsDeck.remove(0));
        }

        // Create a new player and add to the list
        HumanPlayer newPlayer = new HumanPlayer(playerName, playerHand);
        players.add(newPlayer);

        System.out.println("Player " + playerName + " has been added to game!");
        System.out.println("number of players currently: " + players.size());
        System.out.println("number of cards after creating player: "
                + clientsDeck.size());
    }

    /**
     * Creates a new {@link ComputerPlayer} to the game.
     * 
     * @param clientsDeck Contains a subset of {@link Deck} that will be used
     *                    to create the {@link Player}'s initial hand.
     */
    public void createPlayer(List<Card> clientsDeck) {
        System.out.println("number of cards before creating player: "
                + clientsDeck.size());
        List<Card> playerHand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            playerHand.add(clientsDeck.remove(0));
        }

        // Create a new player and add to the list
        ComputerPlayer newPlayer = new ComputerPlayer(playerHand);
        players.add(newPlayer);

        System.out.println("Player " + newPlayer.getName()
                + " has been added to game!");
        System.out.println("number of players currently: " + players.size());
        System.out.println("number of cards after creating player: "
                + clientsDeck.size());
    }

    /**
     * Allows a {@link Player} to choose and play a {@link Card}.
     * 
     * @param player {@link Player} who is currently playing.
     * @return {@link Card} that is played.
     */
    public Card playCard(Player player) {
        if (player instanceof HumanPlayer hPlayer) {
            return hPlayer.playCard(paradeLine);

        } else {
            // Computer player logic
            ConsoleController.consoleDelay(1500);
            Card cardComputerChose = ((ComputerPlayer) player).playCard(paradeLine);
            System.out.println(player.getName() + " has played the card ");
            System.out.println(AsciiArt.printLine(List.of(cardComputerChose)));
            ConsoleController.consoleDelay(2000);
            return cardComputerChose;
        }
    }

    /**
     * Checks if the game has ended. The game has ended if a {@link Player} has
     * collected at least 1 {@link Card} per color, or {@link Deck} is empty.
     * 
     * @return {@code true} if any of the game-ending conditions are met, and
     *         {@code false} otherwise.
     */
    public boolean isGameEnded() {
        // Check if any player has all colors
        for (Player player : players) {
            if (player.hasAllColor()) {
                if (!finalRound) {
                    finalRound = true;
                    System.out.println("\nFinal Round begins! "
                            + player.getName() + " has collected all colors!");
                    // Continue with final round
                    return false; 
                }
            }
        }

        // Check if deck is empty
        if (deck.isDeckEmpty() && !finalRound) {
            finalRound = true;
            System.out.println("\nFinal Round begins! The deck is empty!");
            return false; // Continue with final round
        }

        // If in final round, check if all players have 4 or fewer cards
        if (finalRound) {
            for (Player player : players) {
                if (player.getHand().size() > 4) {
                    return false;
                }
            }

            // All players have 4 or fewer cards, game ends
            return true; 
        }

        return false;
    }

    /**
     * Finds the winner.
     * 
     * @return {@link Player} who won
     */
    public List<Player> getWinner() {
        // Calculate final scores
        Map<Player, Integer> finalScores = calculateScores();

        // Find player with lowest score
        List<Player> winners = new ArrayList<>();
        int lowestScore = Integer.MAX_VALUE;

        for (Map.Entry<Player, Integer> entry : finalScores.entrySet()) {
            if (entry.getValue() < lowestScore) {
                winners.clear();
                lowestScore = entry.getValue();
                winners.add(entry.getKey());
            } else if (entry.getValue() == lowestScore) {
                winners.add(entry.getKey());
            }
        }

        return winners;
    }

    /**
     * Calculate the scores for each {@link Player}.
     * 
     * @return Map that maps the {@link Player} to their score
     */
    private Map<String, Integer> getColorMaxPoint() {
        Map<String, Integer> storeForColorMax = new HashMap<>();
        for (Player p : players) {
            Map<String, Pair<Integer, Integer>> scores = p.calculateScore();
            for (Map.Entry<String, Pair<Integer, Integer>> m : scores.entrySet()) {
                storeForColorMax.putIfAbsent(m.getKey(), 0);
                /*
                 * This gets the maximum amount of Cards for this color
                 * amongst all the players
                 */
                if (storeForColorMax.get(m.getKey()) <= m.getValue().getKey()) {
                    storeForColorMax.put(m.getKey(), m.getValue().getKey());
                }
            }
        }

        return storeForColorMax;
    }

    /**
     * Checks whether all {@link Player}s have the same amount of
     * {@link Card}s.
     * 
     * @param storeForColorMax A map of the card colours and the max amount of
     *                         cards any player has
     * @return Map of String and Boolean that returns {@code true} if all
     *         players do have the same amount of cards of that colour.
     */
    private Map<String, Boolean> allPlayersHasMax(
            Map<String, Integer> storeForColorMax) {
        Map<String, Boolean> allPlayers = new HashMap<String, Boolean>();
        List<String> colors = new ArrayList<>(List.of("red", "blue", "purple",
                "green", "grey", "orange"));
        for (String s : colors) {
            int counter = 0;
            for (Player p : players) {
                Map<String, Pair<Integer, Integer>> colorPerScore = p.calculateScore();
                if (colorPerScore.get(s).getKey() == storeForColorMax.get(s)) {
                    counter++;
                }
            }

            if (counter == players.size()) {
                allPlayers.put(s, true);
            } else {
                allPlayers.put(s, false);
            }

        }

        for (String s : colors) {
            if (!allPlayers.containsKey(s)) {
                allPlayers.put(s, false);
            }
        }
        return allPlayers;
    }

    /**
     * Calculates and returns the score for each {@link Player}.
     * 
     * @return The score for each {@link Player}.
     */
    public Map<Player, Integer> calculateScores() {
        Map<Player, Integer> scoresPerPlayer = new HashMap<Player, Integer>();

        if (players.size() == 2) {
            Player player1 = players.get(0);
            Player player2 = players.get(1);
            scoresPerPlayer.put(player1, 0);
            scoresPerPlayer.put(player2, 0);

            // Get the scores of both players
            Map<String, Pair<Integer, Integer>> p1 = player1.calculateScore();
            Map<String, Pair<Integer, Integer>> p2 = player2.calculateScore();

            // Key -> Color, Pair key -> num of cards, val -> points of cards
            for (Map.Entry<String, Pair<Integer, Integer>> m : p1.entrySet()) {
                int p1CardAmount = m.getValue().getKey();
                int p2CardAmount = p2.get(m.getKey()).getKey();

                int p1Points = m.getValue().getValue();
                int p2Points = p2.get(m.getKey()).getValue();

                if (Math.abs(p1CardAmount - p2CardAmount) <= 1) {
                    scoresPerPlayer.put(player1, scoresPerPlayer.get(player1)
                            + p1Points);
                    scoresPerPlayer.put(player2, scoresPerPlayer.get(player2)
                            + p2Points);
                } else if (p1CardAmount > p2CardAmount) {
                    scoresPerPlayer.put(player1, scoresPerPlayer.get(player1)
                            + p1CardAmount);
                    scoresPerPlayer.put(player2, scoresPerPlayer.get(player2)
                            + p2Points);
                } else {
                    scoresPerPlayer.put(player1, scoresPerPlayer.get(player1)
                            + p1Points);
                    scoresPerPlayer.put(player2, scoresPerPlayer.get(player2)
                            + p2CardAmount);
                }
            }

            return scoresPerPlayer;
        }

        // Note the key of the pair is the amount of cards while the value of
        // the pair is the amount of points from the cards
        Map<String, Integer> storeForColorMax = getColorMaxPoint();
        Map<String, Boolean> allPlayers = allPlayersHasMax(storeForColorMax);

        // Calculate the score of the person
        for (Player p : players) {
            scoresPerPlayer.putIfAbsent(p, 0);
            Map<String, Pair<Integer, Integer>> scores = p.calculateScore();
            for (Entry<String, Pair<Integer, Integer>> m : scores.entrySet()) {
                if (m.getValue().getKey() == storeForColorMax.get(m.getKey())
                        && !allPlayers.get(m.getKey())) {
                    scoresPerPlayer.put(p, scoresPerPlayer.get(p)
                            + m.getValue().getKey());
                } else {
                    scoresPerPlayer.put(p, scoresPerPlayer.get(p)
                            + m.getValue().getValue());
                }
            }
        }
        return scoresPerPlayer;
    }

    /**
     * Initializes the variables for the game.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void initializeGame(Consumer<String> output) {
        // Initialize variables
        playerTurn = 0;
        turnNumber = 1;
        gameEnded = false;
        skipToFinal = false;

        ConsoleController.clearConsole();

        // Initialize game state
        finalRound = false;

        // Initialize score board
        scoreBoard.clear();
        for (Player player : players) {
            scoreBoard.put(player, 0);
        }
    }

    /**
     * Displays the ASCII art for the turn info.
     * 
     * @param output   Method used to output the ASCII art. Either
     *                 {@code server::broadcastMessage} or
     *                 {@code System.out::println}.
     * @param username Unique username of the current player.
     */
    public void displayTurnInfo(Consumer<String> output, String username) {
        if (!finalRound) {
            output.accept(AsciiArt.declarePlayerRound(username,
                    "Turn " + turnNumber));
        } else {
            output.accept(AsciiArt.declarePlayerRound(username,
                    " Final Turn"));
        }
    }

    /**
     * Displays the current parade line.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void displayCurrentParadeLine(Consumer<String> output) {
        output.accept("\nCurrent Parade Line:\n");

        ConsoleController.consoleDelay(1000);
        output.accept(AsciiArt.printLine(paradeLine.getParadeLineCards()));

        ConsoleController.consoleDelay(1000);
    }

    /**
     * Displays every {@link Player}'s collected {@link Card}s.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void displayAllCollectedCards(Consumer<String> output) {
        for (Player p : players) {
            ConsoleController.consoleDelay(500);
            output.accept(p.getName() + "'s collected cards");
            String collectedCards = p.displayCollected();

            if (collectedCards.length() == 0) {
                output.accept(AsciiArt.printEmptyCardCollection());
                } else {
                output.accept(p.displayCollected());
            }
        }
    }

    /**
     * Displays the ASCII art for the final round.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void displayFinalRoundAscii(Consumer<String> output) {
        ConsoleController.clearConsole();
        output.accept(AsciiArt.finalRound());
        ConsoleController.consoleDelay(3000);
        playerTurn = 0;
        skipToFinal = true;
    }

    /**
     * Asks all {@link Player}s to discard 2 cards.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void discardTwoCards(Consumer<String> output) {
        for (Player p : players) {
            output.accept(AsciiArt.declarePlayerRound(p.getName(),
                    "Discard 2 cards!"));
            if (p instanceof HumanPlayer hPlayer) {
                int[] cardsChosenToDiscard = chooseCardsToDiscard(hPlayer);
                hPlayer.discardCardsAtFinalRound(cardsChosenToDiscard[0],
                        cardsChosenToDiscard[1]);
            } else if (p instanceof ComputerPlayer cPlayer) {
                cPlayer.discardCardsAtFinalRound();
            }
        }
    }

    /**
     * Display the scores of all {@link Player}s.
     * 
     * @param output Method used to output the ASCII art. Either
     *               {@code server::broadcastMessage} or
     *               {@code System.out::println}.
     */
    public void displayScores(Consumer<String> output) {
        for (Map.Entry<Player, Integer> entry : calculateScores().entrySet()) {
            output.accept(entry.getKey().getName() + "'s score: "
                    + entry.getValue());
        }
    }

    /**
     * Displays the winner of the game.
     * 
     * @param paradeWinner List of {@link Player}s who won.
     * @param winnerNames  String containing the names of {@link Player}s who
     *                     won.
     * @param output       Method used to output the ASCII art. Either
     *                     {@code server::broadcastMessage} or
     *                     {@code System.out::println}.
     */
    public void displayWinner(List<Player> paradeWinner, String winnerNames,
            Consumer<String> output) {
        if (paradeWinner.size() == players.size()) {
            output.accept("Oh no, a massive tie");
        } else if (paradeWinner.size() == 1) {
            output.accept(winnerNames + " has won parade");
                } else {
            output.accept(winnerNames + " have won parade!");
        }
    }

    /**
     * Starts a game played between {@link Player}s. This method contains
     * the game loop.
     * 
     * @throws IOException Occurs when there is error in handling a turn when
     *                     playing against other {@link HumanPlayer}s. This
     *                     Exception is thrown when there is an invalid input.
     */
    public abstract void startGame() throws IOException;

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
    public abstract List<Card> handlePlayerTurn(Player player)
            throws IOException;

    /**
     * Asks the {@link HumanPlayer}s to discard 2 {@link Card}s from their
     * hands.
     * 
     * @param player Target {@link HumanPlayer}.
     * @return The 2 indicies of the {@link Card}s to discard.
     */
    public abstract int[] chooseCardsToDiscard(HumanPlayer player);
}
