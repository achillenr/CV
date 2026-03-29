package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import com.github.forax.zen.PointerEvent;
import hero.Hero;
import map.Dungeon;
import service.ScoreManager;

public class GameOverView implements GameView {
    private final Hero hero;
    private final int floor, finalScore;
    private boolean done = false;
    private GameView nextView;

    private UIButton restartButton, hofButton, quitButton;

    private record UIButton(int x, int y, int w, int h, String label) {
        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    /**
     * Constructs a GameOverView.
     * 
     * @param hero  The hero who died.
     * @param floor The floor reached.
     */
    public GameOverView(Hero hero, int floor) {
        this.hero = Objects.requireNonNull(hero, "hero cannot be null");
        this.floor = floor;
        this.finalScore = ScoreManager.calculateScore(hero, floor);
        ScoreManager.saveScore(System.getProperty("user.name", "Hero"), finalScore, floor);
    }

    /**
     * Default constructor for GameOverView.
     */
    public GameOverView() {
        this(new Hero(), 0);
    }

    /**
     * Renders the game over screen.
     */
    @Override
    public void draw(Graphics2D g, int width, int height) {
        drawBackground(g, width, height);
        drawStats(g, width);
        setupAndDrawButtons(g, width);
    }

    /**
     * Draws the background image.
     */
    private void drawBackground(Graphics2D g, int w, int h) {
        BufferedImage bg = ImageManager.getImage("death.png");
        GameView.drawFullScreenBackground(g, bg, 0, 0, w, h, Color.BLACK);
    }

    /**
     * Draws the hero stats (score, floor, gold).
     */
    private void drawStats(Graphics2D g, int width) {
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(22f));
        int yStats = 350;

        drawCenteredString(g, "Final Score : " + finalScore, width, yStats);
        drawCenteredString(g, "Floor Reached : " + floor, width, yStats + 40);
        drawCenteredString(g, "Gold Collected : " + hero.getGold(), width, yStats + 80);
    }

    /**
     * Draws a string centered horizontally.
     */
    private void drawCenteredString(Graphics2D g, String text, int width, int y) {
        int x = (width - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    /**
     * Sets up button positions and draws them.
     */
    private void setupAndDrawButtons(Graphics2D g, int width) {
        int btnW = 250, btnH = 45;
        int btnX = (width - btnW) / 2;
        int startBtnY = 520;

        restartButton = new UIButton(btnX, startBtnY, btnW, btnH, "RESTART");
        hofButton = new UIButton(btnX, startBtnY + 60, btnW, btnH, "HALL OF FAME");
        quitButton = new UIButton(btnX, startBtnY + 120, btnW, btnH, "QUIT");

        drawButton(g, restartButton, new Color(50, 150, 50, 200));
        drawButton(g, hofButton, new Color(50, 50, 150, 200));
        drawButton(g, quitButton, new Color(150, 50, 50, 200));
    }

    /**
     * Draws a button.
     */
    private void drawButton(Graphics2D g, UIButton b, Color bg) {
        g.setColor(bg);
        g.fillRoundRect(b.x(), b.y(), b.w(), b.h(), 15, 15);
        g.setColor(Color.WHITE);
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRoundRect(b.x(), b.y(), b.w(), b.h(), 15, 15);
        drawButtonLabel(g, b);
    }

    /**
     * Draws the label on a button.
     */
    private void drawButtonLabel(Graphics2D g, UIButton b) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        int tw = g.getFontMetrics().stringWidth(b.label());
        g.drawString(b.label(), b.x() + (b.w() - tw) / 2, b.y() + 28);
    }

    /**
     * Handles pointer events for buttons.
     */
    @Override
    public void handlePointerEvent(PointerEvent event) {
        if (event.action() != PointerEvent.Action.POINTER_DOWN)
            return;
        int mx = (int) event.location().x();
        int my = (int) event.location().y();
        checkButtons(mx, my);
    }

    /**
     * Checks if a point is inside a button and reacts accordingly.
     */
    private void checkButtons(int mx, int my) {
        if (restartButton != null && restartButton.contains(mx, my)) {
            nextView = new DungeonView(new Dungeon(), new Hero());
            done = true;
        } else if (hofButton != null && hofButton.contains(mx, my)) {
            nextView = new HallOfFameView(this);
            done = true;
        } else if (quitButton != null && quitButton.contains(mx, my)) {
            System.exit(0);
        }
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
     * Resets the view state.
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
