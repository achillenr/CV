package display;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

/**
 * Interface for a graphical view.
 * Each view can draw itself, handle input, and update its logic.
 */
public interface GameView {
    int PADDING = 20;

    /**
     * Draws a progress bar (health, experience, etc.).
     * 
     * @param g           Graphics context
     * @param x           X coordinate
     * @param y           Y coordinate
     * @param width       Bar width
     * @param height      Bar height
     * @param fillPercent Fill percentage (0.0 to 1.0)
     * @param fillColor   Color of the filled portion
     * @param bgColor     Background color of the bar
     * @param withBorder  Whether to draw a white border
     */
    static void drawProgressBar(Graphics2D g, int x, int y, int width, int height,
            float fillPercent, java.awt.Color fillColor,
            java.awt.Color bgColor, boolean withBorder) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(fillColor, "fillColor cannot be null");
        java.util.Objects.requireNonNull(bgColor, "bgColor cannot be null");
        // Background
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        // Fill
        if (fillPercent > 0) {
            g.setColor(fillColor);
            int fillWidth = (int) (width * Math.max(0, Math.min(1.0, fillPercent)));
            g.fillRect(x, y, fillWidth, height);
        }

        // Border
        if (withBorder) {
            g.setColor(java.awt.Color.WHITE);
            g.drawRect(x, y, width, height);
        }
    }

    /**
     * Draws a panel with semi-transparent background and rounded corners.
     * 
     * @param g            Graphics context
     * @param x            X coordinate
     * @param y            Y coordinate
     * @param width        Panel width
     * @param height       Panel height
     * @param bgColor      Background color (usually with alpha)
     * @param borderColor  Border color
     * @param cornerRadius Radius for rounded corners
     */
    static void drawPanel(Graphics2D g, int x, int y, int width, int height,
            java.awt.Color bgColor, java.awt.Color borderColor, int cornerRadius) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(bgColor, "bgColor cannot be null");
        // Background
        g.setColor(bgColor);
        g.fillRoundRect(x, y, width, height, cornerRadius, cornerRadius);

        // Border
        if (borderColor != null) {
            g.setColor(borderColor);
            g.drawRoundRect(x, y, width, height, cornerRadius, cornerRadius);
        }
    }

    /**
     * Draws text centered horizontally at the given X coordinate.
     * 
     * @param g       Graphics context
     * @param text    Text to draw
     * @param centerX X coordinate to center on
     * @param y       Y coordinate (baseline)
     */
    static void drawCenteredText(Graphics2D g, String text, int centerX, int y) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(text, "text cannot be null");
        java.awt.FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.drawString(text, x, y);
    }

    /**
     * Draws the view on the provided Graphics2D context.
     * 
     * @param g      The graphics context to draw on.
     * @param width  The width of the viewport/component.
     * @param height The height of the viewport/component.
     */
    void draw(Graphics2D g, int width, int height);

    /**
     * Handles pointer (mouse/touch) events.
     * 
     * @param event The pointer event data.
     */
    void handlePointerEvent(PointerEvent event);

    /**
     * Handles keyboard events.
     * 
     * @param event The keyboard event data.
     */
    void handleKeyBoardEvent(KeyboardEvent event);

    /**
     * Updates game logic for this view.
     * Called once per frame or at a fixed interval.
     */
    void updateLogic();

    /**
     * Returns true if the view has finished its execution and should transition.
     * 
     * @return true if done.
     */
    boolean isDone();

    /**
     * Returns the next view to display if the current view is done.
     * 
     * @return The next Graphics view instance, or null if none.
     */
    default GameView nextView() {
        return null;
    }

    /**
     * Resets the view to its initial state.
     */
    default void reset() {
    }

    /**
     * Returns the color associated with a given rarity level.
     * Provides consistent color scheme across all views.
     */
    static java.awt.Color getColorByRarity(item.Rarity rarity) {
        java.util.Objects.requireNonNull(rarity, "rarity cannot be null");
        return switch (rarity) {
            case COMMON -> new java.awt.Color(180, 180, 180);
            case RARE -> new java.awt.Color(100, 200, 255);
            case EPIC -> new java.awt.Color(180, 100, 255);
            case LEGENDARY -> new java.awt.Color(255, 215, 0);
            case KEY -> new java.awt.Color(255, 255, 100);
            case GOLD -> new java.awt.Color(255, 255, 0);
            case CURSE -> new java.awt.Color(150, 0, 0);
        };
    }

    /**
     * Finds the top-left anchor position of an item in the backpack grid.
     * Returns the anchor point accounting for shape offset.
     * 
     * @param bp   The backpack to search in
     * @param item The item to find
     * @return Point with grid coordinates (anchor point), or null if not found
     */
    static java.awt.Point findItemGridPosition(item.BackPack bp, item.Item it) {
        java.util.Objects.requireNonNull(bp, "bp cannot be null");
        java.util.Objects.requireNonNull(it, "it cannot be null");
        int minGridX = Integer.MAX_VALUE;
        int minGridY = Integer.MAX_VALUE;
        boolean found = false;

        for (int x = 0; x < item.BackPack.getMaxWidth(); x++) {
            for (int y = 0; y < item.BackPack.getMaxHeight(); y++) {
                if (bp.getItemAt(x, y) != null && bp.getItemAt(x, y).id() == it.id()) {
                    minGridX = Math.min(minGridX, x);
                    minGridY = Math.min(minGridY, y);
                    found = true;
                }
            }
        }
        if (!found)
            return null;

        item.Shape s = it.shape();
        java.awt.Point shapeOffset = findShapeBoundingBox(s);

        // Anchor = MinGrid - MinShape
        return new java.awt.Point(minGridX - shapeOffset.x, minGridY - shapeOffset.y);
    }

    /**
     * Finds the top-left offset of an item's shape within its own matrix.
     * This is used to align the item's texture/drawing with its actual occupied
     * cells.
     *
     * @param shape The item's shape matrix.
     * @return A Point representing the minimum (x, y) coordinates where the shape
     *         has a true value.
     */
    private static java.awt.Point findShapeBoundingBox(item.Shape shape) {
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
        return new java.awt.Point(minShapeX, minShapeY);
    }

    /**
     * Draws the colored background cells for an item's shape.
     * 
     * @param g        Graphics context
     * @param it       The item to draw
     * @param x        Screen X coordinate
     * @param y        Screen Y coordinate
     * @param cellSize Size of each cell in pixels
     * @param selected Whether the item is currently selected
     */
    static void drawItemShapeBackground(Graphics2D g, item.Item it, int x, int y, int cellSize,
            boolean selected) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(it, "it cannot be null");

        item.Shape shape = it.shape();
        java.awt.Point shapeOffset = findShapeBoundingBox(shape);
        java.awt.Color rarityColor = getColorByRarity(it.rarity());

        for (int i = 0; i < shape.width(); i++) {
            for (int j = 0; j < shape.height(); j++) {
                if (shape.get(i, j)) {
                    int drawX = x + (i - shapeOffset.x) * cellSize;
                    int drawY = y + (j - shapeOffset.y) * cellSize;

                    // Multi-layer background
                    g.setColor(new java.awt.Color(0, 0, 0, 150));
                    g.fillRect(drawX, drawY, cellSize, cellSize);

                    g.setColor(new java.awt.Color(rarityColor.getRed(), rarityColor.getGreen(),
                            rarityColor.getBlue(), selected ? 150 : 80));
                    g.fillRect(drawX + 2, drawY + 2, cellSize - 4, cellSize - 4);

                    g.setColor(new java.awt.Color(255, 255, 255, 50));
                    g.drawRect(drawX, drawY, cellSize, cellSize);
                }
            }
        }
    }

    /**
     * Draws the texture image for an item.
     * Falls back to a placeholder if the texture is missing.
     * 
     * @param g        Graphics context
     * @param it       The item to draw
     * @param x        Screen X coordinate
     * @param y        Screen Y coordinate
     * @param cellSize Size of each cell in pixels
     */
    static void drawItemTexture(Graphics2D g, item.Item it, int x, int y, int cellSize) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(it, "it cannot be null");

        BufferedImage texture = ImageManager.getImage(it.getTexturePath());
        item.Shape shape = it.shape();

        if (texture != null) {
            java.awt.Point shapeOffset = findShapeBoundingBox(shape);

            int drawX = x - (shapeOffset.x * cellSize);
            int drawY = y - (shapeOffset.y * cellSize);
            int drawW = shape.width() * cellSize;
            int drawH = shape.height() * cellSize;

            g.drawImage(texture, drawX, drawY, drawW, drawH, null);
        } else {
            drawMissingTextureFallback(g, x, y, cellSize, shape);
        }
    }

    /**
     * Draws a fallback placeholder when an item texture is missing.
     * 
     * @param g        Graphics context
     * @param x        Screen X coordinate
     * @param y        Screen Y coordinate
     * @param cellSize Size of each cell in pixels
     * @param shape    The item's shape
     */
    static void drawMissingTextureFallback(Graphics2D g, int x, int y, int cellSize, item.Shape shape) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(shape, "shape cannot be null");
        g.setColor(new java.awt.Color(255, 0, 0, 100));
        g.fillRect(x + 5, y + 5, shape.width() * cellSize - 10, shape.height() * cellSize - 10);
        g.setColor(java.awt.Color.WHITE);
        g.drawString("?", x + cellSize / 2, y + cellSize / 2);
    }

    /**
     * Draws a full-screen background image.
     * Falls back to a solid color if the image is null.
     */
    static void drawFullScreenBackground(Graphics2D g, BufferedImage image,
            int x, int y, int width, int height,
            java.awt.Color fallbackColor) {
        java.util.Objects.requireNonNull(g, "g cannot be null");
        java.util.Objects.requireNonNull(fallbackColor, "fallbackColor cannot be null");
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(fallbackColor);
            g.fillRect(x, y, width, height);
        }
    }
}
