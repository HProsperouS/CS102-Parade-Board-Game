package app.user;

import app.MultiplayerController;
import app.ParadeApp;
import app.SinglePlayerController;
import app.board.*;
import java.util.*;
import utility.AsciiArt;
import utility.ConsoleController;

/**
 * HumanPlayer represents a user who is playing the game. It extends the {@link Player} class
 * and provides functionality for human interaction in the game. The human player is instantiated
 * when a user chooses to play in either {@link SinglePlayerController} or {@link MultiplayerController}.
 * 
 * The human player has the following features:
 * 1. A bankroll for betting in the game
 * 2. A wager system for placing bets
 * 3. Interactive methods for playing cards and managing the hand
 * 
 * @author Amos
 */
public class HumanPlayer extends Player {

    /** The player's current balance for betting. */
    private double bankroll;
    
    /** The current bet amount. */
    private double wager;

    /** Reads user input. */
    protected Scanner scanner = ParadeApp.scanner;

    /**
     * Creates a new human player with the specified name and starting hand.
     * Initializes the bankroll to 1000.
     * 
     * @param name  The player's name
     * @param cards The initial hand of cards
     */
    public HumanPlayer(String name, List<Card> cards) {
        super(name, cards);
        bankroll = 1000;
    }

    /**
     * Returns a formatted string representation of the player's hand.
     * Uses {@link AsciiArt#printLine} to format the output.
     * 
     * @return Formatted string of the player's hand
     */
    public String displayHand() {
        return AsciiArt.printLine(this.getHand());
    }

    /**
     * Sets the player's current wager amount.
     * 
     * @param wager The new wager amount
     */
    public void setWager(double wager) {
        this.wager = wager;
    }

    /**
     * Gets the player's current wager amount.
     * 
     * @return The current wager
     */
    public double getWager() {
        return wager;
    }

    /**
     * Gets the player's current bankroll.
     * 
     * @return The current bankroll
     */
    public double getBankroll() {
        return bankroll;
    }

    /**
     * Modifies the player's bankroll by the specified amount.
     * Positive values increase the bankroll, negative values decrease it.
     * 
     * @param change The amount to change the bankroll by
     */
    public void modifyBankroll(double change) {
        bankroll += change;
    }

    @Override
    public Card playCard(ParadeLine pline) {
         List<Card> playerHand = this.getHand();
         int cardIndex;
 
         while (true) {
             try {
                 // Ask player for card
                 ConsoleController.clearInputBufferForPvAI();
                 System.out.print("Choose a card to play (1-" + playerHand.size()
                         + "): ");
                 if (scanner.hasNext()) {
                     String rangeErrorMessage = "Invalid choice! Please enter a "
                             + "number between 1 and "
                             + playerHand.size();
                     if (scanner.hasNextInt()) {
                         cardIndex = scanner.nextInt() - 1;
                         // Consume newline character
                         scanner.nextLine(); 
 
                         if (cardIndex >= 0 && cardIndex < playerHand.size()) {
                             System.out.println(AsciiArt.gameLoading());
                             Card cardToPlay = playerHand.get(cardIndex);
                             playerHand.remove(cardIndex);
                             return cardToPlay; // Valid choice
                         } else {
                             System.out.println(rangeErrorMessage);
                         }
                     } else {
                         // If user enters a non-integer value (e.g., letters)
                         System.out.println(rangeErrorMessage);
                         scanner.next(); // Discard invalid input
                     }
                 } else {
                     // Handle case where user presses Ctrl + C
                     ConsoleController.terminateConsoleOperation();
                 }
             } catch (Exception e) {
                 System.out.println("Invalid input! "
                         + "Please enter a valid number.");
                 scanner.nextLine();
             }
         }
     }
}