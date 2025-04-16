package app.board;

/**
 * Card is the representation of an actual card in the Parade game. It contains
 * the color and value of the card. 
 * 
 * @author Wei Shen
 */
public class Card {

    private int value; 
    private String color; 

    /**
     * Constructor for Card.
     * 
     * @param value Number on the card.
     * @param color Color of the card.
     */
    public Card(int value, String color) {
        this.value = value;
        this.color = color;
    }

    /**
     * Getter method for the value.
     * 
     * @return Integer representing the number on the card.
     */
    public int getValue() {
        return value;
    }

    /**
     * Getter method for the color.
     * 
     * @return String representing the color of the card.
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns a String object representing this Card.
     * 
     * @return A string representation of the card.
     */
    @Override
    public String toString() {
        return "Card [value=" + value + ", color=" + color + "]";
    }

    /**
     * Returns the hash code value for this card.
     * 
     * @return The hash code value for this card.
     */
    @Override
    public int hashCode() {
        return this.color.hashCode() + this.value;
    }
}
