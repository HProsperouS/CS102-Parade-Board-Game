package utility;

/**
 * A Pair class contains a key-value pair.
 * 
 * @param <T> The type of the key.
 * @param <U> The type of the value.
 * @author Joseph
 */
public class Pair<T, U> {

    private T key;
    private U value;

    /** Constructs a new Pair with {@code null} key and value. */
    public Pair() {
        this(null, null);
    }

    /**
     * Constructs a new Pair with the specified key and value.
     * 
     * @param key   The key of the pair.
     * @param value The value associated with the key.
     */
    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Getter method for {@code key}.
     * 
     * @return The key of this pair object.
     */
    public T getKey() {
        return this.key;
    }

    /**
     * Setter method for {@code key}.
     * 
     * @param key The key to set. 
     */
    public void setKey(T key) {
        this.key = key;
    }

    /**
     * Getter method for {@code value}.
     * 
     * @return the value
     */
    public U getValue() {
        return this.value;
    }

    /**
     * Setter method for {@code value}.
     * 
     * @param value the value to set
     */
    public void setValue(U value) {
        this.value = value;
    }
}
