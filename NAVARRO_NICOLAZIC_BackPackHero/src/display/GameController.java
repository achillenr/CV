package display;

import java.awt.Color;
import java.awt.Graphics2D;
import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import java.util.Objects;

/**
 * Central controller responsible for the game loop,
 * event dispatching, and view transitions.
 */
public class GameController {

    private GameView activeView;

    /**
     * Creates a controller with the initial view.
     *
     * @param initialView first view to be displayed
     */
    public GameController(GameView initialView) {
        this.activeView = Objects.requireNonNull(initialView, "initialView cannot be null");
    }

    /**
     * Starts the application and launches the game loop.
     *
     * @param initialView first view to be displayed
     */
    public static void start(GameView initialView) {
        Objects.requireNonNull(initialView, "initialView cannot be null");
        Application.run(Color.BLACK, context -> {
            GameController controller = new GameController(initialView);
            runGameLoop(context, controller);
        });
    }

    /**
     * Runs the main game loop.
     */
    private static void runGameLoop(ApplicationContext context, GameController controller) {
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(controller, "controller cannot be null");
        while (true) {
            handleEvents(context, controller);
            updateAndRender(context, controller);
        }
    }

    /**
     * Polls and dispatches all pending input events.
     */
    private static void handleEvents(ApplicationContext context, GameController controller) {
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(controller, "controller cannot be null");
        Event event = context.pollEvent();
        while (event != null) {
            dispatchEvent(event, controller);
            event = context.pollEvent();
        }
    }

    /**
     * Forwards an event to the active view.
     */
    private static void dispatchEvent(Event event, GameController controller) {
        Objects.requireNonNull(event, "event cannot be null");
        Objects.requireNonNull(controller, "controller cannot be null");
        switch (event) {
            case KeyboardEvent k -> controller.activeView.handleKeyBoardEvent(k);
            case PointerEvent p -> controller.activeView.handlePointerEvent(p);
            default -> {
            }
        }
    }

    /**
     * Updates game logic and renders the current frame.
     */
    private static void updateAndRender(ApplicationContext context, GameController controller) {
        Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(controller, "controller cannot be null");
        controller.activeView.updateLogic();
        checkViewTransition(controller);
        context.renderFrame(g -> controller.draw(g, context));
    }

    /**
     * Checks whether the active view should be replaced.
     */
    private static void checkViewTransition(GameController controller) {
        Objects.requireNonNull(controller, "controller cannot be null");
        if (controller.activeView.isDone()) {
            changeView(controller);
        }
    }

    /**
     * Switches to the next view if available.
     */
    private static void changeView(GameController controller) {
        Objects.requireNonNull(controller, "controller cannot be null");
        GameView next = controller.activeView.nextView();
        if (next == null)
            return;

        controller.activeView = next;
        controller.activeView.reset();
    }

    /**
     * Draws the background and the active view.
     */
    private void draw(Graphics2D g, ApplicationContext context) {
        Objects.requireNonNull(g, "graphics context cannot be null");
        Objects.requireNonNull(context, "application context cannot be null");
        var screenInfo = context.getScreenInfo();
        int w = screenInfo.width();
        int h = screenInfo.height();

        drawBackground(g, w, h);
        activeView.draw(g, w, h);
    }

    /**
     * Clears the screen.
     */
    private void drawBackground(Graphics2D g, int w, int h) {
        Objects.requireNonNull(g, "graphics context cannot be null");
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, w, h);
    }
}
