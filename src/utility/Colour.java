package utility;

/**
 * Colour contains the colour encoding for the colours used in the app.  
 * 
 * @author Joseph
 */
public enum Colour {
    /** Colour encoding for red. */
    RED("\033[0;31m"), 

    /** Colour encoding for blue. */
    BLUE("\033[34m"), 

    /** Colour encoding for orange. */
    ORANGE("\033[38;5;208m"), 

    /** Colour encoding for green. */
    GREEN("\033[32m"),

    /** Colour encoding for purple. */
    PURPLE("\033[35m"), 
    
    /** Coluor encoding for grey. */
    GREY("\033[90m"); 

    /** Colour encoding of default console. */
    private static final String RESET = "\033[0m";
    private final String value;

    /**
     * Constructor for Color.
     * 
     * @param value 
     */
    Colour(String value) {
        this.value = value;
    }

    /**
     * Takes in a colour and returns its corresponding encoding.
     * 
     * @param colour Colour to encode.
     * @return Encoding for the corresponding colour. 
     */
    public String apply(String colour) {
        return value + colour + RESET;
    }
}
