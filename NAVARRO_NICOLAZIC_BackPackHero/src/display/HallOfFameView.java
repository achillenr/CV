package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import com.github.forax.zen.PointerEvent;
import com.github.forax.zen.KeyboardEvent;
import java.util.Objects;
import service.ScoreManager;

public class HallOfFameView implements GameView {
    private final GameView parentView;
    private boolean done = false;
    private GameView nextView;
    private final List<ScoreManager.ScoreEntry> scores;
    private final UIButton quitButton = new UIButton(50, 530, 180, 45, "BACK");

    private record UIButton(int x, int y, int w, int h, String label) {
        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    /**
     * Constructs a HallOfFameView.
     * 
     * @param parentView The view to return to.
     */
    public HallOfFameView(GameView parentView) {
        this.parentView = Objects.requireNonNull(parentView, "parentView cannot be null");
        this.scores = ScoreManager.loadScores();
    }

    /**
     * Renders the Hall of Fame screen.
     */
    @Override
    public void draw(Graphics2D g, int width, int height) {
        drawBackground(g, width, height);
        drawPodium(g, width, height);
        drawButton(g, quitButton, new Color(100, 50, 50, 200));
    }

    /**
     * Draws the background image.
     */
    private void drawBackground(Graphics2D g, int width, int height) {
        BufferedImage bg = ImageManager.getImage("halloffame.png");
        GameView.drawFullScreenBackground(g, bg, 0, 0, width, height, new Color(20, 20, 30));
    }

    /**
     * Draws the top 3 scores on the podium.
     */
    private void drawPodium(Graphics2D g, int w, int h) {
        drawEntry(g, getScore(0), w / 2, (int) (h * 0.72), 1);
        drawEntry(g, getScore(1), (int) (w * 0.32), (int) (h * 0.765), 2);
        drawEntry(g, getScore(2), (int) (w * 0.68), (int) (h * 0.785), 3);
    }

    /**
     * Retrieves a score entry by its rank index.
     */
    private ScoreManager.ScoreEntry getScore(int index) {
        return (index < scores.size()) ? scores.get(index) : null;
    }

    /**
     * Draws a single score entry.
     */
    private void drawEntry(Graphics2D g, ScoreManager.ScoreEntry entry, int x, int y, int rank) {
        String name = (entry != null) ? entry.name() : "---";
        String score = ((entry != null) ? entry.score() : 0) + " pts";

        drawTextWithShadow(g, name, x, y, 22f, Color.WHITE, 0);
        drawTextWithShadow(g, score, x, y + 30, 18f, getRankColor(rank), 2);
    }

    /**
     * Draws text with a shadow for better visibility.
     */
    private void drawTextWithShadow(Graphics2D g, String text, int x, int y, float size, Color c, int yOff) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, size));
        int w = g.getFontMetrics().stringWidth(text);

        g.setColor(Color.BLACK);
        g.drawString(text, x - w / 2 + 2, y + yOff + 2); // Shadow
        g.setColor(c);
        g.drawString(text, x - w / 2, y + yOff); // Text
    }

    /**
     * Gets the color corresponding to a rank.
     */
    private Color getRankColor(int rank) {
        return switch (rank) {
            case 1 -> new Color(255, 215, 0); // Gold
            case 2 -> new Color(192, 192, 192); // Silver
            case 3 -> new Color(205, 127, 50); // Bronze
            default -> Color.WHITE;
        };
    }

    /**
     * Draws a UI button.
     */
    private void drawButton(Graphics2D g, UIButton b, Color bg) {
        g.setColor(bg);
        g.fillRoundRect(b.x(), b.y(), b.w(), b.h(), 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(b.x(), b.y(), b.w(), b.h(), 10, 10);

        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        int tw = g.getFontMetrics().stringWidth(b.label());
        g.drawString(b.label(), b.x() + (b.w() - tw) / 2, b.y() + 26);
    }

    /**
     * Handles pointer events for the back button.
     */
    @Override
    public void handlePointerEvent(PointerEvent event) {
        if (event.action() != PointerEvent.Action.POINTER_DOWN)
            return;
        if (quitButton.contains((int) event.location().x(), (int) event.location().y())) {
            quit();
        }
    }

    /**
     * Handles keyboard events (ESC to quit).
     */
    @Override
    public void handleKeyBoardEvent(KeyboardEvent e) {
        if (e.key() == KeyboardEvent.Key.ESCAPE)
            quit();
    }

    /**
     * Prepares to transition back to the parent view.
     */
    private void quit() {
        nextView = parentView;
        done = true;
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
     * Updates logic (unused).
     */
    @Override
    public void updateLogic() {
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
