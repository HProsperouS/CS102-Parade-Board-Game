package app.user;

import java.util.*;
import app.board.*;
import app.*;

/**
 * ComputerPlayer is a simulated player in Parade. It extends the {@link Player} class
 * and implements AI logic for playing the game. The computer player is instantiated
 * when a human player chooses to play against AI in either {@link SinglePlayerController}
 * or {@link MultiplayerController}.
 * 
 * The computer player uses the following strategies:
 * 1. When playing a card: Selects the card that will result in the lowest possible score
 *   by analyzing the current state of the {@link ParadeLine}
 * 2. When discarding cards: Chooses cards to discard based on their color and value to
 *   minimize the final score
 * 
 * @author Joseph
 */
public class ComputerPlayer extends Player {

    /** Counter used to generate unique AI player names (e.g., "AI 1", "AI 2", etc.). */
    public static int botCounter = 1;

    /**
     * Creates a new computer player with the specified starting hand.
     * The player's name will be automatically generated using the botCounter.
     * 
     * @param cards The initial hand of cards for the computer player
     */
    public ComputerPlayer(List<Card> cards) {
        super("AI " + botCounter, cards);
        botCounter++;
    }

    /**
     * Selects a card to play from the computer's hand. The selection is based on
     * calculating the potential score for each card in the hand and choosing the
     * one that would result in the lowest score.
     * 
     * The algorithm:
     * 1. For each card in hand, calculate the total value of cards that would be
     *    collected from the parade line
     * 2. Select the card that results in the lowest total value
     * 
     * @param paradeLineCards The current state of the parade line
     * @return The selected card to play
     */
    @Override
    public Card playCard(ParadeLine paradeLineCards) {
        // Maps card index to score
        Map<Integer, Integer> indexScore = new HashMap<Integer, Integer>();
        
        List<Card> computerCurrentHand = super.getHand();
        // Calculate potential score for each card in hand
        for (int i = 0; i < computerCurrentHand.size(); i++) {
            String cardColor = computerCurrentHand.get(i).getColor();
            int cardVal = computerCurrentHand.get(i).getValue();
            indexScore.put(i, 0);

            // Calculate score based on matching color or value
            List<Card> paradeLine = paradeLineCards.getParadeLineCards();
            for (int j = 0; j < paradeLine.size() - cardVal; j++) {
                Card currCard = paradeLine.get(j);
                if (cardColor.equals(currCard.getColor())
                        || cardVal >= currCard.getValue()) {
                    indexScore.put(i, indexScore.get(i) + currCard.getValue());
                }
            }
        }

        // Find card with lowest potential score
        int cardIndex = 0;
        int leastScore = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> m : indexScore.entrySet()) {
            if (m.getValue() < leastScore) {
                leastScore = m.getValue();
                cardIndex = m.getKey();
            }
        }

        // Remove and return selected card
        List<Card> playerHand = super.getHand();
        Card cardToPlay = playerHand.get(cardIndex);
        playerHand.remove(cardIndex);
        return cardToPlay;
    }

    /**
     * Selects two cards to discard from the computer's hand at the end of the game.
     * The selection is based on:
     * 1. First trying to maintain color majorities in the collected cards
     * 2. If no color majority can be maintained, selecting the lowest value cards
     * 
     * The selected cards are added to the player's collection, while the remaining
     * cards are discarded.
     */
    public void discardCardsAtFinalRound() {
        List<Card> cardsCollected = super.getCollectedCards();
        System.out.println(cardsCollected.toString());

        // Count cards per color in collection
        Map<String, Integer> amountPerColor = new HashMap<>();
        for (Card c : cardsCollected) {
            int cardAmt = amountPerColor.getOrDefault(c.getColor(), 0);
            amountPerColor.put(c.getColor(), cardAmt + 1);
        }

        List<Card> handCards = super.getHand();
        List<Card> undiscarded = new ArrayList<>();
        
        // Select two cards to keep
        for (int i = 0; i < 2; i++) {
            Card currCard = null;
            int max = Integer.MIN_VALUE;

            // Try to maintain color majority
            for (Card c : handCards) {
                if (amountPerColor.get(c.getColor()) != null) {
                    int val = amountPerColor.get(c.getColor());
                    if (max < val) {
                        max = val;
                        currCard = c;
                        amountPerColor.put(c.getColor(), val + 1);
                    }
                }
            }

            // If no color majority possible, select lowest value card
            if (currCard == null) {
                int minVal = Integer.MAX_VALUE;
                for (Card c : handCards) {
                    if (c.getValue() < minVal) {
                        currCard = c;
                        minVal = c.getValue();
                    }
                }
            }
            handCards.remove(currCard);
            undiscarded.add(currCard);
        }

        this.addToCollection(undiscarded);
    }
}