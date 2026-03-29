package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import hero.Hero;
import item.BackPack;
import item.Curse;
import item.Item;
import item.Shape;
import main.Combat;
import java.awt.Point;
import java.awt.image.BufferedImage;

public final class CursePlacementView implements GameView {

    private final Combat combat;
    private final GameView parentView;

    private BackpackMetrics backpackMetrics;
    private int lastMouseX;
    private int lastMouseY;
    private boolean done = false;

    /**
     * Constructs a CursePlacementView.
     * 
     * @param combat     The ongoing combat.
     * @param parentView The view to return to.
     */
    public CursePlacementView(Combat combat, GameView parentView) {
        this.combat = Objects.requireNonNull(combat);
        this.parentView = Objects.requireNonNull(parentView);
    }

    /* ===================== DRAW ===================== */

    /**
     * Renders the curse placement screen.
     */
    @Override
    public void draw(Graphics2D g, int width, int height) {
        Hero hero = combat.getHero();
        BackPack bp = hero.getBackPack();

        // 1. Draw background
        BufferedImage bg = ImageManager.getImage("cursebg.png");
        GameView.drawFullScreenBackground(g, bg, 0, 0, width, height, Color.BLACK);

        // 2. Adjust for padding
        int contentWidth = width - (2 * GameView.PADDING);
        int contentHeight = height - (2 * GameView.PADDING);

        g.translate(GameView.PADDING, GameView.PADDING);

        drawBackpack(g, contentWidth, contentHeight, bp);
        drawUI(g);

        g.translate(-GameView.PADDING, -GameView.PADDING);
    }

    /**
     * Draws the backpack grid and current items.
     */
    private void drawBackpack(Graphics2D g, int screenW, int screenH, BackPack bp) {
        backpackMetrics = calculateBackpackMetrics(screenW, screenH, bp);

        int startX = backpackMetrics.startX();
        int startY = backpackMetrics.startY();
        int cell = backpackMetrics.cellSize();

        // Grid
        for (int y = 0; y < backpackMetrics.rows(); y++) {
            for (int x = 0; x < backpackMetrics.cols(); x++) {
                if (!bp.isActive(x, y)) {
                    continue;
                }
                int px = startX + x * cell;
                int py = startY + y * cell;

                g.setColor(new Color(60, 60, 60, 150));
                g.fillRect(px, py, cell, cell);

                g.setColor(Color.GRAY);
                g.drawRect(px, py, cell, cell);
            }
        }

        // Existing items
        for (Item item : bp.getItems()) {
            drawItem(g, bp, item);
        }

        // Curse preview
        Curse curse = combat.getPendingCurse();
        if (curse != null) {
            drawCursePreview(g, curse);
        }

        g.setColor(Color.WHITE);
        g.drawString("PLACE THE CURSE", startX, startY - 10);
    }

    /**
     * Draws an item in the backpack.
     */
    private void drawItem(Graphics2D g, BackPack bp, Item item) {
        if (backpackMetrics == null)
            return;

        Point screenPos = calculateItemScreenPosition(bp, item);
        if (screenPos == null)
            return;
        drawItemTexture(g, item, screenPos);
    }

    /**
     * Calculates the screen position for an item.
     */
    private Point calculateItemScreenPosition(BackPack bp, Item item) {
        // Grid coordinates
        Point gridPos = findItemGridPosition(bp, item);
        if (gridPos == null)
            return null;

        // Shape offset
        Point shapeOffset = findShapeOffset(item.shape());

        // Final pixel calculation
        int cell = backpackMetrics.cellSize();
        int finalX = backpackMetrics.startX() + (gridPos.x - shapeOffset.x) * cell;
        int finalY = backpackMetrics.startY() + (gridPos.y - shapeOffset.y) * cell;

        return new Point(finalX, finalY);
    }

    /**
     * Finds the logical top-left grid position of an item.
     */
    private Point findItemGridPosition(BackPack bp, Item item) {
        return GameView.findItemGridPosition(bp, item);
    }

    /**
     * Finds the internal shape offset (first true cell).
     */
    private Point findShapeOffset(Shape shape) {
        int minShapeX = Integer.MAX_VALUE;
        int minShapeY = Integer.MAX_VALUE;

        for (int i = 0; i < shape.width(); i++) {
            for (int j = 0; j < shape.height(); j++) {
                if (shape.get(i, j)) {
                    minShapeX = Math.min(minShapeX, i);
                    minShapeY = Math.min(minShapeY, j);
                }
            }
        }
        return new Point(minShapeX, minShapeY);
    }

    /**
     * Displays the item texture.
     */
    private void drawItemTexture(Graphics2D g, Item item, Point pos) {
        String path = item.getTexturePath();
        BufferedImage img = ImageManager.getImage(path);
        int cell = backpackMetrics.cellSize();

        if (img != null) {
            g.drawImage(img, pos.x, pos.y,
                    item.shape().width() * cell,
                    item.shape().height() * cell, null);
        }
    }

    /**
     * Renders a preview of where the curse will be placed.
     */
    private void drawCursePreview(Graphics2D g, Curse curse) {
        int cell = backpackMetrics.cellSize();
        int gx = (lastMouseX - backpackMetrics.startX()) / cell;
        int gy = (lastMouseY - backpackMetrics.startY()) / cell;

        boolean canPlace = canPlaceCurse(curse, gx, gy);
        drawCurseHighlight(g, curse, gx, gy, cell, canPlace);
    }

    private boolean canPlaceCurse(Curse curse, int gx, int gy) {
        Shape shape = curse.shape();
        for (int dx = 0; dx < shape.width(); dx++) {
            for (int dy = 0; dy < shape.height(); dy++) {
                if (shape.get(dx, dy)) {
                    int cx = gx + dx, cy = gy + dy;
                    if (cx < 0 || cy < 0 || cx >= backpackMetrics.cols() || cy >= backpackMetrics.rows()
                            || !combat.getHero().getBackPack().isActive(cx, cy)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void drawCurseHighlight(Graphics2D g, Curse curse, int gx, int gy, int cell, boolean canPlace) {
        g.setColor(canPlace ? new Color(255, 0, 255, 120) : new Color(255, 0, 0, 120));
        Shape shape = curse.shape();
        for (int dx = 0; dx < shape.width(); dx++) {
            for (int dy = 0; dy < shape.height(); dy++) {
                if (shape.get(dx, dy)) {
                    int px = backpackMetrics.startX() + (gx + dx) * cell;
                    int py = backpackMetrics.startY() + (gy + dy) * cell;
                    g.fillRect(px + 1, py + 1, cell - 2, cell - 2);
                }
            }
        }
    }

    /**
     * Draws instructional UI text.
     */
    private void drawUI(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.drawString("Click to place the curse", 30, 40);
        g.drawString("Overlaid items will be destroyed", 30, 60);
    }

    /* ===================== INPUT ===================== */

    @Override
    public void handlePointerEvent(PointerEvent event) {
        lastMouseX = (int) event.location().x();
        lastMouseY = (int) event.location().y();

        if (event.action() == PointerEvent.Action.POINTER_DOWN) {
            tryPlaceCurse();
        }
    }

    private void tryPlaceCurse() {
        if (backpackMetrics == null || combat.getPendingCurse() == null)
            return;

        int cell = backpackMetrics.cellSize();
        int gx = (lastMouseX - backpackMetrics.startX()) / cell;
        int gy = (lastMouseY - backpackMetrics.startY()) / cell;

        if (combat.placePendingCurse(combat.getHero().getBackPack(), gx, gy)) {
            done = true;
        }
    }

    /* ===================== LOOP ===================== */

    /**
     * Updates logic (unused).
     */
    @Override
    public void updateLogic() {
        // Nothing to do
    }

    /**
     * Checks if placement is done.
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the next view (back to combat).
     */
    @Override
    public GameView nextView() {
        if (done) {
            return parentView;
        }
        return this;
    }

    /* ===================== METRICS ===================== */

    /**
     * Calculates rendering metrics for the placement grid.
     */
    private BackpackMetrics calculateBackpackMetrics(int screenW, int screenH, BackPack bp) {
        int cols = bp.getWidth();
        int rows = bp.getHeight();
        int cellSize = Math.min(screenW / (cols + 4), screenH / (rows + 4));
        int gridW = cols * cellSize;
        int gridH = rows * cellSize;
        int startX = (screenW - gridW) / 2;
        int startY = (screenH - gridH) / 2;
        return new BackpackMetrics(startX, startY, cellSize, cols, rows);
    }

    private record BackpackMetrics(int startX, int startY, int cellSize, int cols, int rows) {
    }

    @Override
    public void handleKeyBoardEvent(KeyboardEvent event) {
        // TODO Auto-generated method stub

    }
}
