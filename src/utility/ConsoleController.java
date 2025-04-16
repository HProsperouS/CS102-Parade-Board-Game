package utility;

import java.io.BufferedReader;
import java.io.IOException;

import net.Client;

/**
 * ConsoleController handles certain features regarding the console. Such
 * features include clearing the console, implementing a console delay, and
 * handling a force exit.
 * 
 * @author Wei Bin
 */
public class ConsoleController {

    /** Clears the console. */
    public static void clearConsole() {
        System.out.print("\033c");
        System.out.flush();
    }

    /**
     * Implements a delay on the console.
     * 
     * @param time Number of seconds to sleep.
     */
    public static void consoleDelay(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            System.out.println("error in delaying console.");
        }
    }

    /** Handles when the user tries to terminate the application. */
    public static void terminateConsoleOperation() {
        System.out.println();
        System.out.println("You have pressed Ctrl + C!");
        System.exit(0);
    }

    /**
     * Clears any existing user input. This method is used for when playing 
     * agianst AI.
     */
    public static void clearInputBufferForPvAI() {
        try {
            while (System.in.available() > 0) {
                // Discard input byte-by-byte
                System.in.read(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears any existing user input. This method is used for when playing 
     * against other players. 
     * 
     * @param reader Reader for a {@link Client}.
     */
    public static void clearInputBufferForPvP(BufferedReader reader) {
        try {
            while (reader.ready()) {
                // Discard the line
                reader.readLine(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
