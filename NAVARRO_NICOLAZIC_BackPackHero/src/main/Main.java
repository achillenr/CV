package main;

/**
 * Application entry point.
 */
public class Main {

    /**
     * Starts the game by initializing the first view
     * and launching the game controller.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        java.util.Objects.requireNonNull(args, "args cannot be null");
        display.GameView initialView = new display.StartMenuView();
        System.out.println("Starting game...");

        display.GameController.start(initialView);
        System.exit(0);
    }
}
