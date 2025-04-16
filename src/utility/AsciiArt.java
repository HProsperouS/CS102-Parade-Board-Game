package utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.board.Card;
import app.board.ParadeLine;
import app.user.Player;

/**
 * AsciiArt holds ASCII art that will be printed to the console at the start of
 * the game and at every turn. This class includes ASCII art for cards, game messages,
 * and special game modes like BlackJack.
 * 
 * @author Wei Bin
 */
public class AsciiArt {
    // Card ASCII art templates for each color
    private static final Map<String, String> CARD_TEMPLATES = Map.of(
        "RED", """
              ┌──────────┐, \
              │ %-2d       │, \
              │  ██████  │, \
              │  (•_•)   │, \
              │  /  | \\\\ │, \
              │        %-2d│, \
              └──────────┘""",
        "BLUE", """
              ┌──────────┐, \
              │ %-2d       │, \
              │ ( ^_^ )  │, \
              │  <( )>   │, \
              │          │, \
              │        %-2d│, \
              └──────────┘""",
        "ORANGE", """
              ┌──────────┐, \
              │ %-2d       │, \
              │  /\\\\_/\\\\ │, \
              │  ( o.o ) │, \
              │   > ^ <  │, \
              │        %-2d│, \
              └──────────┘""",
        "GREEN", """
              ┌──────────┐, \
              │ %-2d       │, \
              │  (\\\\_/)  │, \
              │  (='.'=) │, \
              │  (")(")  │, \
              │        %-2d│, \
              └──────────┘""",
        "PURPLE", """
              ┌──────────┐, \
              │ %-2d       │, \
              │  _____   │, \
              │  ( o_o ) │, \
              │  /| |\\\\  │, \
              │        %-2d│, \
              └──────────┘""",
        "GREY", """
              ┌──────────┐, \
              │ %-2d       │, \
              │   (°v°)  │, \
              │   <( )>  │, \
              │  ~~~~~~  │, \
              │        %-2d│, \
              └──────────┘"""
    );

    /** 
     * Stores card templates for each color.
     * Each color has a list of 7 strings that represent the ASCII art for each line of the card.
     * These templates are used to build the visual representation of cards in the game.
     */
    public static Map<String, List<String>> colorToCard = mapColorToCard();

    /**
     * Get the ASCII art template for a specific card color.
     * 
     * @param color The color of the card
     * @return String containing the ASCII art template
     */
    public static String getCardTemplate(String color) {
        return CARD_TEMPLATES.get(color);
    }


    /**
     * Call getCardTemplate method to map the color to the strings of the
     * {@link Card}.
     * 
     * @return Map of color to the String representation of the {@link Card}.
     */
    private static Map<String, List<String>> mapColorToCard() {
        Map<String, List<String>> colorToCard = new HashMap<>();
        String[] colorArr = { "RED", "BLUE", "ORANGE", "GREEN", "PURPLE",
                "GREY" };
        for (String color : colorArr) {
            String cardString = getCardTemplate(color);
            List<String> cardPrintList = List.of(cardString.split(","));
            colorToCard.putIfAbsent(color.toLowerCase(), cardPrintList);
        }
        return colorToCard;
    }

    /**
     * Prints a horizontal line of {@link Card}s. If a card is {@code null}, a
     * blank space will be printed instead.
     *
     * @param partOfList A sublist of {@link Card}s to be printed in one row.
     * @return A formatted string representing the row of {@link Card}s.
     */
    public static String getCardPrinted(List<Card> partOfList) {
        String res = "";
        for (int i = 0; i < 7; i++) {
            // Since our card design has the height of 7
            for (int j = 0; j < partOfList.size(); j++) {
                Card currCard = partOfList.get(j);
                if (currCard == null) {
                    res += "            ";
                } else {
                    String currLine = formatCardLine(i, currCard.getValue(),
                            currCard.getColor());
                    res += Colour.valueOf(currCard.getColor().toUpperCase())
                            .apply(currLine);
                }
                res += "    ";
            }
            res += "\n";
        }
        return res;
    }

    /**
     * This handles the card printing for {@link ParadeLine} and for the
     * {@link Player}'s hand.
     *
     * @param listOfCard Contains the amount of {@link Card}s needed to be
     *                   printed.
     * @return String containing the printed cards.
     **/
    public static String printLine(List<Card> listOfCard) {
        if (listOfCard == null) {
            return "Please provide a list of cards";
        }
        String res = "";

        List<Card> listSegment = new ArrayList<Card>();
        for (int i = 0; i < listOfCard.size(); i++) {
            if (i != 0 && i % 6 == 0) {
                res += getCardPrinted(listSegment);
                listSegment.clear();
                listSegment.add(listOfCard.get(i));
            } else {
                listSegment.add(listOfCard.get(i));
            }
        }
        if (listSegment.size() != 0) {
            res += getCardPrinted(listSegment);
        }
        return res;
    }

    /**
     * Formats a specific line of a card's ASCII art representation.
     * This function handles the formatting of individual lines of a card's display,
     * applying the card's value to the appropriate lines (1 and 5) and returning
     * the formatted line with any trailing whitespace removed. Ultimately, this function's
     * high-level function is to format the String representation of the line of cards properly
     * for printLine to print the cards out correctly.
     *
     * @param lineNo The line number (0-6) of the card's ASCII art to format
     * @param value  The numeric value of the card to be displayed
     * @param color  The color of the card (e.g., "red", "blue")
     * @return A formatted string representing the specified line of the card
     */
    public static String formatCardLine(int lineNo, int value, String color) {
        if (lineNo == 1 || lineNo == 5) {
            return colorToCard.get(color).get(lineNo).formatted(value).strip();
        }
        return colorToCard.get(color).get(lineNo).strip();
    }

    /**
     * Returns a string representing an empty collection zone.
     * 
     * @return A string representation of an empty collection zone.
     */
    public static String printEmptyCardCollection() {
        return "\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                + "\u2557\n" +
                "\u2551  EMPTY  \u2551\n" +
                "\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                + "\u255D\n";
    }

    /**
     * Returns a loading string.
     * 
     * @return The loading screen.
     */
    public static String gameProcessing() {
        StringBuilder output = new StringBuilder("Game is Processing");
        String[] stages = {

                "█▒▒▒▒▒▒▒▒▒ 10%",
                "███▒▒▒▒▒▒▒ 30%",
                "██████▒▒▒▒ 60%",
                "██████████ 100%"
        };

        output.append("...\n");

        for (String stage : stages) {
            output.append("       ").append(stage).append("\n");
        }

        output.append(" Ready to Play!\n\n");
        return output.toString();
    }

    /**
     * Returns the loading message.
     * 
     * @return The loading message.
     */
    public static String gameLoading() {
        return "Game is Processing...\n";
    }

    /**
     * Returns which {@link Player}'s turn it is.
     * 
     * @param playerName Unique username of {@link Player}.
     * @param turn       Current turn.
     * @return ASCII art with the current {@link Player}'s name.
     */
    public static String declarePlayerRound(String playerName, String turn) {
        String text = "  " + playerName + "'s Turn, " + turn + "  ";
        int length = text.length();
        return "\u2554" + "═".repeat(length) + "\u2557\n" +
                "\u2551" + " ".repeat(length) + "\u2551\n" +
                "\u2551" + text + "\u2551\n" +
                "\u2551" + " ".repeat(length) + "\u2551\n" +
                "\u255A" + "═".repeat(length) + "\u255D\n";
    }

    /**
     * Returns the ASCII art with a message.
     * 
     * @param message Message to declare.
     * @return ASCII art with message.
     */
    public static String declareMessage(String message) {
        int length = message.length();
        return "\u2554" + "═".repeat(length) + "\u2557\n" +
                "\u2551" + " ".repeat(length) + "\u2551\n" +
                "\u2551" + message + "\u2551\n" +
                "\u2551" + " ".repeat(length) + "\u2551\n" +
                "\u255A" + "═".repeat(length) + "\u255D\n";
    }

    /**
     * Returns a colorful banner for the BlackJack game mode.
     * 
     * @return A colorful ASCII art banner for the blackjack game.
     */
    public static String getBlackJackBanner() {
        return "\n"
                + Colour.RED.apply("                ")
                + Colour.GREEN.apply(" B L A C K J A C K ")
                + Colour.RED.apply("         ")
                + "\n"
                + Colour.RED.apply("┌────────────────")
                + Colour.GREEN.apply("────────────────")
                + Colour.RED.apply("───────────────┐")
                + "\n"
                + Colour.RED.apply("│                ")
                + Colour.GREEN.apply("                 ")
                + Colour.RED.apply("              │")
                + "\n"
                + Colour.RED.apply("│                ")
                + Colour.GREEN.apply("                 ")
                + Colour.RED.apply("              │")
                + "\n"
                + Colour.RED.apply("│                ")
                + Colour.GREEN.apply("                 ")
                + Colour.RED.apply("              │")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply(" __   _               _                  _     ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply("/|/  \\ |               | |  /\\            | |  ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply(" | __/ |     __   __   | |   |  __   __   | |  ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply(" |   \\ |    /  | /     |/_)  | /  | /     |/_  ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply("/|(__//|__//\\_/|_\\___//| \\_  |/\\_/|_\\___//| \\_ ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply("                            /|                 ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply("                            \\|                 ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + "                                               "
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("│")
                + Colour.GREEN.apply("                TARGET SCORE: ")
                + Colour.RED.apply("15               ")
                + Colour.RED.apply("│")
                + "\n"
                + Colour.RED.apply("└────────────────")
                + Colour.GREEN.apply("────────────────")
                + Colour.RED.apply("───────────────┘")
                + "\n";
    }

    /**
     * Returns the ASCII art for displaying current bankrolls.
     * 
     * @return ASCII art for bankroll display header
     */
    public static String getBankrollHeader() {
        return "\n"
                + Colour.RED.apply("  ╔══════════════════════════════╗  ")
                + "\n"
                + Colour.RED.apply("  ║                              ║  ")
                + "\n"
                + Colour.RED.apply("  ║")
                + Colour.GREEN.apply("      Current Bankrolls       ")
                + Colour.RED.apply("║  ")
                + "\n"
                + Colour.RED.apply("  ║                              ║  ")
                + "\n"
                + Colour.RED.apply("  ╚══════════════════════════════╝  ");
    }

    /**
     * Returns the ASCII art for the final round.
     * 
     * @return ASCII art for the final round.
     */
    public static String finalRound() {
        return "███████╗██╗███╗   ██╗ █████╗ ██╗         "
                + "██████╗  ██████╗ ██╗   ██╗███╗   ██╗██████╗ \n"
                + "██╔════╝██║████╗  ██║██╔══██╗██║         "
                + "██╔══██╗██╔═══██╗██║   ██║████╗  ██║██╔══██╗\n"
                + "█████╗  ██║██╔██╗ ██║███████║██║         "
                + "██████╔╝██║   ██║██║   ██║██╔██╗ ██║██║  ██║\n"
                + "██╔══╝  ██║██║╚██╗██║██╔══██║██║         "
                + "██╔══██╗██║   ██║██║   ██║██║╚██╗██║██║  ██║\n"
                + "██║     ██║██║ ╚████║██║  ██║███████╗    "
                + "██║  ██║╚██████╔╝╚██████╔╝██║ ╚████║██████╔╝\n"
                + "╚═╝     ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    "
                + "╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝╚═════╝ \n";
    }
}      
