package app.board;

import java.util.ArrayList;
import java.util.List;
import utility.AsciiArt;

/**
 * ParadeLine represents the board where players will play their {@link Card}s. 
 * It handles part of the game logic, namely which {@link Card}s the 
 * player will collect each turn.
 * 
 * @author Wei Bin
 */
public class ParadeLine {

    private List<Card> paradeCards; 

    /**
     * Constructor for ParadeLine. 
     * 
     * @param initialCards Initial 6 {@link Card}s on the Parade.
     */
    public ParadeLine(List<Card> initialCards) {
        this.paradeCards = new ArrayList<>(initialCards);
    }

    /**
     * Get the current {@link Card}s in the parade line.
     * 
     * @return List of {@link Card}s in the parade line.
     */
    public List<Card> getParadeLineCards() {
        return paradeCards;
    }

    /**
     * Add a new {@link Card} to the parade line.
     * 
     * @param card {@link Card} to add.
     */
    public void addCard(Card card) {
        paradeCards.add(card);
    }

    /**
     * From the card played, {@code card}, find its value. Skip that number of 
     * cards in the parade line and return all {@link Card}s that fulfills any 
     * of the following two conditions:
     *     1. Value is less than equal to {@code card}'s value
     *     2. Color is the same as {@code card}'s value
     * 
     * These cards are removed from the parade line and returned. 
     * 
     * @param card {@link Card} that is played. 
     * @return List of {@link Card}s that is collected.
     **/
    public List<Card> collectCardFromParade(Card card) {
        // Holds the collected cards
        List<Card> playerCollectedCards = new ArrayList<Card>();

        for (int i = 0; i < paradeCards.size() - card.getValue(); i++) {
            // Check for the two conditions, color and value
            // If the card is the same color or the value is less than equal to the played card's value
            if (paradeCards.get(i).getColor().equals(card.getColor())
                    || paradeCards.get(i).getValue() <= card.getValue()) {
                playerCollectedCards.add(paradeCards.get(i));
            }
        }

        // Remove cards from the parade line
        for (Card t : playerCollectedCards) {
            if (paradeCards.contains(t)) {
                paradeCards.remove(t);
            }
        }

        // Add the card played to the parade line
        paradeCards.add(card);

        return playerCollectedCards;
    }

    /**
     * Returns a String representation of the parade line.
     * 
     * @return String showing all {@link Card}s in the parade line.
     */
    @Override
    public String toString() {
        return AsciiArt.printLine(paradeCards);
    }
}