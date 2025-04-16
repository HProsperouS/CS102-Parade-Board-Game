package app.board;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Deck represents the deck of {@link Card}s in the Parade game. {@link Card}s 
 * are drawn from the deck.
 * 
 * @author Wei Shen
 */
public class Deck {

    private List<Card> deckCards; 

    // Array containing the colors from the game
    private static final String[] COLORLIST = { "red", "blue", "purple",
            "green", "grey", "orange" };

    /**
     * Constructor for Deck. It creates a new deck with all 66 {@link Card}s. 
     * There are 6 colors, and the value can be from 0 to 10 (inclusive).
     */
    public Deck() {
        deckCards = new ArrayList<>();

        // Number 0 - 10 for each card.
        for (String color : COLORLIST) {
            for (int value = 0; value <= 10; value++) {
                // Creates a new card, then add it to the deck
                deckCards.add(new Card(value, color));
            }
        }

        // Shuffle the deck once its created
        shuffle();
    }

    /** Shuffles the deck randomly. */
    public void shuffle() {
        Collections.shuffle(deckCards);
    }

    /**
     * Draws the top {@link Card} from the deck. The top {@link Card} is 
     * removed and returned.
     * 
     * @return The top {@link Card} of the deck.
     */
    public Card drawCard() {
        // draw cards from the front of the cards list
        return deckCards.remove(0);
    }

    /**
     * Checks if the deck is empty.
     * 
     * @return {@code true} if the deck is empty, {@code false} otherwise.
     */
    public boolean isDeckEmpty() {
        return deckCards.isEmpty();
    }

    /**
     * Getter method for the deck.
     * 
     * @return A list of {@link Card}s representing the deck.
     */
    public List<Card> getDeck() {
        return deckCards;
    }
}
