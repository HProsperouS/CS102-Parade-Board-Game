package app.user;

import java.util.*;
import app.board.*;
import utility.Pair;
import utility.AsciiArt;

/**
 * Player is the base class for all players in the Parade game. It contains
 * general methods and attributes that are common to both {@link HumanPlayer}
 * and {@link ComputerPlayer}. The Player class manages the player's hand,
 * collected cards, and provides methods for game actions like playing cards
 * and calculating scores.
 * 
 * @author Joseph
 */
public abstract class Player {

    /** List of valid card colors in the game. */
    private List<String> colors;
    
    /** The player's name. */
    private String name;
    
    /** The player's current hand of cards. */
    private List<Card> hand;
    
    /** Cards collected from the parade line. */
    private List<Card> collectedCards;
    
    /**
     * Creates a new player with the specified name and starting hand.
     * Initializes an empty collection zone for collected cards.
     * 
     * @param name The player's name
     * @param hand The initial hand of cards
     */
    public Player(String name, List<Card> hand) {
        this.name = name;
        this.hand = hand;
        this.collectedCards = new ArrayList<Card>();
    }

    /**
     * Plays a card from the player's hand to the parade line.
     * This is an abstract method that must be implemented by subclasses.
     * 
     * @param pLine Current {@link ParadeLine}.
     * @return The card that is played
     */
    public abstract Card playCard(ParadeLine pLine);

    /**
     * Adds cards collected from the parade line to the player's collection zone.
     * 
     * @param cards The cards to add to the collection zone
     */
    public void addToCollection(List<Card> cards) {
        if (cards == null || cards.size() == 0) {
            return;
        }
        cards.stream().forEach(a -> collectedCards.add(a));
    }

    /**
     * Removes a card from the player's hand at the specified index.
     * 
     * @param cardIndex The index of the card to remove
     */
    public void removeCardFromHand(int cardIndex) {
        hand.remove(cardIndex);
    }

    /**
     * Discards 2 cards from the player's hand at the final round.
     * The remaining cards are added to the collection zone.
     * 
     * @param indx1 Index of the first card to discard
     * @param indx2 Index of the second card to discard
     */
    public void discardCardsAtFinalRound(int indx1, int indx2) {
        for (int i = 0; i < hand.size(); i++) {
            if (i != indx1 && i != indx2) {
                collectedCards.add(hand.get(i));
            }
        }
    }

    /**
     * Calculates the player's score based on collected cards.
     * Returns a map where each color is mapped to a pair containing:
     * 1. The number of cards of that color
     * 2. The total points of cards of that color
     * 
     * @return Map of color to card count and point total
     */
    public Map<String, Pair<Integer, Integer>> calculateScore() {
        colors = new ArrayList<>(List.of("red", "blue", "purple", "green",
                "grey", "orange"));
        Map<String, Pair<Integer, Integer>> scorePerColor = new HashMap<String, Pair<Integer, Integer>>();

        // Initialize scores for all colors
        for (String s : colors) {
            scorePerColor.put(s, new Pair<Integer, Integer>(0, 0));
        }

        // Calculate scores for collected cards
        for (Card c : collectedCards) {
            int keyTemp = scorePerColor.get(c.getColor()).getKey();
            int valTemp = scorePerColor.get(c.getColor()).getValue();
            scorePerColor.put(c.getColor(),
                    new Pair<Integer, Integer>(keyTemp + 1,
                            valTemp + c.getValue()));
        }

        return scorePerColor;
    }

    /**
     * Adds a card drawn from the deck to the player's hand.
     * 
     * @param card The card drawn from the deck
     */
    public void getCardFromDeck(Card card) {
        this.hand.add(card);
    }

    /**
     * Checks if the player has collected at least one card of each color.
     * 
     * @return true if all colors are represented, false otherwise
     */
    public boolean hasAllColor() {
        colors = new ArrayList<>(List.of("red", "blue", "purple", "green",
                "grey", "orange"));

        Set<String> store = new HashSet<>();
        for (Card c : collectedCards) {
            store.add(c.getColor());
        }

        return store.size() == colors.size();
    }

    /**
     * Checks if this player is equal to another player.
     * Two players are equal if they have the same name.
     * 
     * @param p The player to compare with
     * @return true if the players have the same name, false otherwise
     */
    public boolean equals(Player p) {
        if (p == null) {
            return false;
        }
        return p.name.equals(name);
    }

    /**
     * Returns a string representation of the collected cards,
     * organized vertically by color.
     * 
     * @return Formatted string of collected cards
     */
    public String displayCollected() {
        String printedLine = "";
        List<Card> collectedCards = getCollectedCards();

        Map<String, List<Card>> store = new HashMap<>();

        // Group cards by color
        for (Card c : collectedCards) {
            store.putIfAbsent(c.getColor(), new ArrayList<Card>());
            store.get(c.getColor()).add(c);
        }

        int counter = 0;
        Set<String> colorKey = store.keySet();
        while (true) {
            List<Card> cardIndx = new ArrayList<Card>();
            Iterator<String> see = colorKey.iterator();
            while (see.hasNext()) {
                String color = (String) see.next();
                if (counter < store.get(color).size()) {
                    cardIndx.add(store.get(color).get(counter));
                } else {
                    cardIndx.add(null);
                }
            }

            boolean allNull = true;
            for (Card c : cardIndx) {
                if (c != null) {
                    allNull = false;
                    break;
                }
            }

            if (allNull) {
                break;
            }

            printedLine += AsciiArt.printLine(cardIndx);
            counter++;
        }

        return printedLine;
    }

    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the player's current hand.
     * 
     * @return The player's hand
     */
    public List<Card> getHand() {
        return this.hand;
    }

    /**
     * Gets the cards collected from the parade line.
     * 
     * @return The list of collected cards
     */
    public List<Card> getCollectedCards() {
        return this.collectedCards;
    }

    /**
     * Generates a hash code based on the player's name.
     * 
     * @return The hash code value for this player
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
