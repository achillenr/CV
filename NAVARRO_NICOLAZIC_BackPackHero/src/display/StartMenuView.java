package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import com.github.forax.zen.PointerEvent;
import hero.Hero;
import map.Dungeon;

/**
 * Start menu view displayed when the application launches.
 * Allows the player to start the game, open the hall of fame, or quit.
 */
public class StartMenuView implements GameView {

    private boolean done = false;
    private GameView nextView;
    private int lastWidth, lastHeight;

    private final UIButton startButton = new UIButton(180, 50, "START GAME");
    private final UIButton hofButton = new UIButton(180, 50, "Hall of Fame");
    private final UIButton quitButton = new UIButton(180, 50, "QUIT");

    /**
     * Simple immutable UI button.
     */
    private record UIButton(int w, int h, String label) {

        /**
         * Checks whether the given coordinates are inside the button.
         */
        boolean contains(int mx, int my, int x, int y) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    /**
     * Renders the start menu.
     */
    @Override
    public void draw(Graphics2D g, int width, int height) {
        this.lastWidth = width;
        this.lastHeight = height;

        drawBackground(g, width, height);
        drawMenuButtons(g, width, height);
    }

    /**
     * Draws the menu background image.
     */
    private void drawBackground(Graphics2D g, int width, int height) {
        BufferedImage bg = ImageManager.getImage("startmenu.png");
        GameView.drawFullScreenBackground(g, bg, 0, 0, width, height, Color.BLACK);
    }

    /**
     * Draws all menu buttons.
     */
    private void drawMenuButtons(Graphics2D g, int width, int height) {
        int startX = calculateStartX(width);
        int y = calculateY(height);
        int gap = 30;

        drawButton(g, startButton, startX, y, new Color(50, 150, 50));
        drawButton(g, hofButton, startX + startButton.w() + gap, y, new Color(50, 50, 150));
        drawButton(g, quitButton, startX + (startButton.w() + gap) * 2, y, new Color(150, 50, 50));
    }

    /**
     * Computes the horizontal starting position of the menu.
     */
    private int calculateStartX(int width) {
        int gap = 30;
        int totalW = startButton.w() * 3 + gap * 2;
        return (width - totalW) / 2;
    }

    /**
     * Computes the vertical position of the menu.
     */
    private int calculateY(int height) {
        return height - 100;
    }

    /**
     * Draws a single button.
     */
    private void drawButton(Graphics2D g, UIButton b, int x, int y, Color bg) {
        drawButtonShape(g, b, x, y, bg);
        drawButtonText(g, b, x, y);
    }

    /**
     * Draws the background shape of a button.
     */
    private void drawButtonShape(Graphics2D g, UIButton b, int x, int y, Color bg) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, b.w(), b.h(), 15, 15);
        g.setColor(bg);
        g.setStroke(new java.awt.BasicStroke(3));
        g.drawRoundRect(x, y, b.w(), b.h(), 15, 15);
    }

    /**
     * Draws the text label of a button.
     */
    private void drawButtonText(Graphics2D g, UIButton b, int x, int y) {
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        int tw = g.getFontMetrics().stringWidth(b.label());
        g.drawString(b.label(), x + (b.w() - tw) / 2, y + 32);
    }

    /**
     * Handles mouse click events on menu buttons.
     */
    @Override
    public void handlePointerEvent(PointerEvent event) {
        if (event.action() != PointerEvent.Action.POINTER_DOWN)
            return;

        int mx = (int) event.location().x();
        int my = (int) event.location().y();
        checkButtonClicks(mx, my);
    }

    /**
     * Executes the action associated with the clicked button.
     */
    private void checkButtonClicks(int mx, int my) {
        int startX = calculateStartX(lastWidth);
        int y = calculateY(lastHeight);
        int gap = 30;

        if (startButton.contains(mx, my, startX, y))
            launchGame();
        else if (hofButton.contains(mx, my, startX + startButton.w() + gap, y))
            openHallOfFame();
        else if (quitButton.contains(mx, my, startX + (startButton.w() + gap) * 2, y))
            System.exit(0);
    }

    /**
     * Switches to the main game view.
     */
    private void launchGame() {
        nextView = new DungeonView(new Dungeon(), new Hero());
        done = true;
    }

    /**
     * Switches to the hall of fame view.
     */
    private void openHallOfFame() {
        nextView = new HallOfFameView(this);
        done = true;
    }

    /**
     * Handles keyboard events (unused).
     */
    @Override
    public void handleKeyBoardEvent(com.github.forax.zen.KeyboardEvent e) {
    }

    /**
     * Updates logic (unused).
     */
    @Override
    public void updateLogic() {
    }

    /**
     * Resets the view state when it becomes active again.
     */
    @Override
    public void reset() {
        this.done = false;
        this.nextView = null;
    }

    /**
     * Returns true if the view is done.
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the next view to transition to.
     */
    @Override
    public GameView nextView() {
        return nextView;
    }
}
