package app;

import app.board.Card;
import app.user.HumanPlayer;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.*;
import net.Server;
import utility.AsciiArt;
import utility.Colour;

/**
 * BlackJack contains the logic to figure out the winner of the Blackjack game.
 * It is relevant when the BlackJack game mode is chosen in {@link ParadeApp}.
 * 
 * The BlackJack class works closely with:
 * 1. {@link HumanPlayer}: Manages player wagers and bankrolls
 * 2. {@link Server}: Handles communication between players
 * 3. {@link MultiplayerController}: Controls the game flow in blackjack mode
 * 
 * Key features:
 * 1. Manages betting system with minimum and maximum bids
 * 2. Calculates winners based on target score
 * 3. Handles wager collection and distribution
 * 4. Tracks player bankrolls throughout the game
 * 
 * @author Josepph, Wei Shen, Amos
 */
public class BlackJack {

    /** Server instance for player communication. */
    private Server server;
    
    /** List of players in the game. */
    private List<HumanPlayer> humanPlayerList;

    /** Contains the minimum bid a {@link HumanPlayer} can make. */
    public static final int MINBID = 10;

    /** Contains the upper threshold for the BlackJack game. */
    public static final int TARGET_SCORE = 15;

    /**
     * Creates a new BlackJack game instance.
     * 
     * @param humanPLayerList List of {@link HumanPlayer}s participating in this game
     * @param server          {@link Server} instance used for communication
     */
    public BlackJack(List<HumanPlayer> humanPLayerList, Server server) {
        this.humanPlayerList = humanPLayerList;
        this.server = server;
    }

    /**
     * Collects wagers from all {@link HumanPlayer}s in the game.
     * Each player's wager must be between {@link #MINBID} and HumanPlayer's total bankroll.
     * The collected wagers are broadcast to all players.
     */
    public void collectWagers() {
        Map<HumanPlayer, Double> playerToWager = new HashMap<>();
        for (HumanPlayer player : humanPlayerList) {
            server.broadcastMessage("\n" +player.getName() + "'s turn to place a bet.");

            server.sendMessage(player.getName(), "Your current bankroll is $"
                    + player.getBankroll()+"0");
            double wager = getWagerFromPlayer(player);
            if(wager == -1){
                server.broadcastMessage("Game ended");
                server.closeServerSocket();
            }
            player.setWager(wager);
            playerToWager.put(player, wager);
        }
        server.broadcastMessage("ALL BETS ARE IN!!!!!");
        for (Map.Entry<HumanPlayer, Double> m : playerToWager.entrySet()) {
            server.broadcastMessage("|| " + m.getKey().getName()
                    + " has placed the bet " + m.getValue() + ". ||");
        }
    }

    /**
     * Retrieves a valid wager from a {@link HumanPlayer}.
     * The wager must be between {@link #MINBID} and HumanPlayer's total bankroll.
     * 
     * @param player The {@link HumanPlayer} to get the wager from
     * @return The valid wager amount
     */
    public double getWagerFromPlayer(HumanPlayer player) {
        String playerName = player.getName();
        double wager = 0;
        boolean validWager = false;
        if (player.getBankroll() < MINBID){
            server.sendMessage(playerName, "You don't have enough money to play blackjack anymore :-(. Focus on parade!");
            return 0;
        }
        while (!validWager) {
            try {
                server.sendMessage(playerName, "\nENTER YOUR WAGER ($" + MINBID
                        + ".00 to $" + player.getBankroll() + "0):");
                wager = server.listenFrom(playerName);
                if(wager == -1){
                        // Player disconnected
                        server.broadcastMessage(playerName
                                + " has disconnected. Game ends!");
                        server.broadcastMessage("All players, "
                                + "press CTRL + C to exit!");
                                validWager = true;
                        throw new IOException("Player disconnected");
                }

                if (wager >= MINBID && wager <= player.getBankroll()) {
                    validWager = true;
                } else if (wager > player.getBankroll()) {
                    server.sendMessage(playerName,
                            "You cannot wager more than your bankroll, $"
                                    + player.getBankroll() + ".\n");
                } else {
                    server.sendMessage(playerName,
                            "\nPLEASE ENTER A WAGER BETWEEN $" + MINBID
                                    + " AND $" + player.getBankroll() + ".\n");
                }
            } catch (Exception e) {
                server.sendMessage(playerName, "Please enter a valid number.");
            }
        }

        return wager;
    }

    /**
     * Displays the current bankrolls of all {@link HumanPlayer}s.
     * Uses {@link Colour} for formatting the output.
     */
    public void printBankRoll() {
        server.broadcastMessage(AsciiArt.getBankrollHeader());

        for (HumanPlayer hp : humanPlayerList) {
            String bankrollMsg = hp.getName() + ": $"
                    + String.format("%.2f", hp.getBankroll());
            server.broadcastMessage(Colour.GREEN.apply(" >>> " + bankrollMsg));
        }
    }

    /**
     * Determines the winner(s) of the current round based on the collected cards.
     * The winner is the player whose total card value is closest to {@link #TARGET_SCORE}
     * without exceeding it. In case of a tie, the winnings are split among the winners.
     * 
     * @param allCollectedCards Map of {@link HumanPlayer}s to their collected {@link Card}s
     * @return List of winning {@link HumanPlayer}s, or null if all players busted
     */
    public List<HumanPlayer> getRoundWinner(Map<HumanPlayer, List<Card>> allCollectedCards) {
        int bestScore = 0;
        int bestDifference = Integer.MAX_VALUE;
        List<HumanPlayer> winnerList = new ArrayList<>();

        server.broadcastMessage("\n===== ROUND IS OVERRRRR !! =====");
        server.broadcastMessage("Target score: " + TARGET_SCORE);

        Set<Entry<HumanPlayer, List<Card>>> set = allCollectedCards.entrySet();
        for (Map.Entry<HumanPlayer, List<Card>> m : set) {
            int score = 0;
            for (Card c : m.getValue()) {
                score += c.getValue();
            }

            if (score <= TARGET_SCORE) {
                int difference = TARGET_SCORE - score;
                if (difference < bestDifference) {
                    winnerList.clear();
                    bestDifference = difference;
                    bestScore = score;
                    winnerList.add(m.getKey());
                } else if (difference == bestDifference) {
                    winnerList.add(m.getKey());
                }
            }
        }

        // If all players over >15
        if (winnerList.size() == 0) {
            server.broadcastMessage("\nAll players busted! "
                    + "No winners this round.");
            printBankRoll();
            return null;
        }

        // If more than 1 person got same score, means tie, return null
        // If there is a tie, everyone keeps their bets, print the bankroll and return null
        if (winnerList.size() == humanPlayerList.size()) {
            server.broadcastMessage("");
            server.broadcastMessage("There is a Tie! Nobody wins this round.");
            printBankRoll();
            return null;
        }
        String winners = winnerList.stream()
                .map(a -> a.getName())
                .collect(Collectors.joining(", "));

        server.broadcastMessage("\n" + winners
                + " wins with a BlackJack score of " + bestScore + "!");

        // run logic if we have a winner
        for (HumanPlayer hp : humanPlayerList) {
            if (winnerList.contains(hp)) {
                hp.modifyBankroll(hp.getWager());
                server.broadcastMessage(hp.getName() + " wins $"
                        + String.format("%.2f", hp.getWager()));
            } else {
                hp.modifyBankroll(-1 * hp.getWager());
                server.broadcastMessage(hp.getName() + " loses $"
                        + String.format("%.2f", hp.getWager()));
            }
        }

        printBankRoll();
        return winnerList;
    }

    /**
     * Determines the overall winner of the BlackJack game.
     * The winner is the {@link HumanPlayer} with the highest bankroll.
     * 
     * @return The winning {@link HumanPlayer}
     */
    public HumanPlayer getBlackjackWinner() {
        HumanPlayer res = null;
        double total = 0;
        for (HumanPlayer hp : humanPlayerList) {
            if (hp.getBankroll() > total) {
                total = hp.getBankroll();
                res = hp;
            }
        }
        return res;
    }

}
