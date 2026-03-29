package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;
import hero.Hero;
import item.BackPack;
import item.Consumable;
import item.Item;

import item.Shape;

public class BackPackView implements GameView {
    private final BackPack backpack;
    private final GameView parentView;
    private final Hero hero;
    private boolean done = false;

    // Drag & Drop state
    private Item draggingItem = null;
    private Item selectedItem = null;
    private int originalGridX = -1;
    private int originalGridY = -1;
    private Point originalOutsidePos = null; // Backup to cancel drag
    private boolean fromOutside = false;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    // Mouse coordinates
    private float currentMouseX = 0;
    private float currentMouseY = 0;

    private GridMetrics lastMetrics;

    // Layout constants
    private static final int PADDING = 20;
    private static final int OUTSIDE_ZONE_OFFSET_Y = 10;
    private static final int OUTSIDE_ZONE_HEIGHT = 700;
    private static final int INFOBOX_WIDTH = 250;
    private static final int INFOBOX_HEIGHT = 300;

    /**
     * Constructs a new BackPackView.
     * 
     * @param backpack   The backpack model to display.
     * @param parentView The view to return to when closing this one.
     * @param hero       The hero object (for consumables and stats).
     */
    public BackPackView(BackPack backpack, GameView parentView, Hero hero) {
        this.backpack = Objects.requireNonNull(backpack);
        this.parentView = Objects.requireNonNull(parentView);
        this.hero = Objects.requireNonNull(hero);
    }

    @Override
    public void draw(Graphics2D g, int width, int height) {
        drawBackground(g, 0, 0, width, height);

        GridMetrics metrics = calculateMetrics(width, height);
        this.lastMetrics = metrics;

        drawOutsideZone(g, metrics);
        drawGrid(g, metrics);
        drawGridItems(g, metrics);
        drawOutsideItems(g, metrics);
        drawDraggingItem(g, metrics);
        drawUnlockInfo(g, width, height);
        drawInfobox(g, width, height);
    }

    private record GridMetrics(int cellSize, int gridWidth, int gridHeight, int startX, int startY,
            int outsideX, int outsideY, int outsideW, int outsideH) {
    }

    /**
     * Calculates the visual layout metrics for the backpack display.
     * 
     * @param w Total screen width.
     * @param h Total screen height.
     * @return A GridMetrics object containing layout calculations.
     */
    private GridMetrics calculateMetrics(int w, int h) {
        int cols = backpack.getWidth();
        int rows = backpack.getHeight();
        int cellSize = calculateCellSize(w, h, cols, rows);

        int gridW = cols * cellSize;
        int gridH = rows * cellSize;
        int startX = (w - gridW) / 2;
        int startY = PADDING + 50;

        int outY = startY + gridH + OUTSIDE_ZONE_OFFSET_Y;
        int outX = PADDING + 50;
        int outW = w - (2 * PADDING) - 100;

        return new GridMetrics(cellSize, gridW, gridH, startX, startY, outX, outY, outW, OUTSIDE_ZONE_HEIGHT);
    }

    private int calculateCellSize(int w, int h, int cols, int rows) {
        int availableHeight = h - (2 * PADDING) - OUTSIDE_ZONE_HEIGHT - OUTSIDE_ZONE_OFFSET_Y;
        int cellSize = Math.min((w - 2 * PADDING) / (cols + 1), availableHeight / (rows + 1));
        if (cellSize < 40)
            cellSize = 40;
        return (int) (cellSize * 1.3);
    }

    /**
     * Draws the background image for the backpack view.
     */
    private void drawBackground(Graphics2D g, int x, int y, int w, int h) {
        // Direct call to ImageManager
        BufferedImage bg = ImageManager.getImage("backpack.png");
        GameView.drawFullScreenBackground(g, bg, x, y, w, h, new Color(30, 30, 30));
    }

    /**
     * Draws the visual representation of the area outside the backpack.
     */
    private void drawOutsideZone(Graphics2D g, GridMetrics m) {
        g.setStroke(new java.awt.BasicStroke(2));
        GameView.drawPanel(g, m.outsideX, m.outsideY, m.outsideW, m.outsideH,
                new Color(20, 20, 20, 180), new Color(100, 100, 100), 15);
        g.setStroke(new java.awt.BasicStroke(1)); // Reset stroke
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        g.drawString("OUTSIDE ITEMS", m.outsideX + 10, m.outsideY + 25);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        g.setColor(new Color(255, 100, 100));
        g.drawString("(Objects not in backpack will be lost at the start of the next fight)", m.outsideX + 130,
                m.outsideY + 25);
    }

    /**
     * Draws the backpack grid (unlocked and unlockable cells).
     */
    private void drawGrid(Graphics2D g, GridMetrics m) {
        List<Point> unlockable = backpack.getUnlockableCells();
        boolean hasPoints = backpack.getAvailableUnlocks() > 0;

        for (int r = 0; r < backpack.getHeight(); r++) {
            for (int c = 0; c < backpack.getWidth(); c++) {
                int px = m.startX + c * m.cellSize;
                int py = m.startY + r * m.cellSize;

                if (backpack.isActive(c, r)) {
                    g.setColor(new Color(60, 60, 60, 150));
                    g.fillRect(px, py, m.cellSize, m.cellSize);
                    g.setColor(new Color(120, 120, 120));
                    g.drawRect(px, py, m.cellSize, m.cellSize);
                } else if (unlockable.contains(new Point(c, r)) && hasPoints) {
                    g.setColor(new Color(50, 200, 50, 50));
                    g.fillRect(px, py, m.cellSize, m.cellSize);
                    g.setColor(new Color(50, 200, 50, 200));
                    g.drawRect(px, py, m.cellSize, m.cellSize);
                }
            }
        }
    }

    /**
     * Draws items currently placed in the grid.
     */
    private void drawGridItems(Graphics2D g, GridMetrics m) {
        Set<Item> drawn = new HashSet<>();
        for (Item item : backpack.getItems()) {
            if (item == draggingItem || drawn.contains(item))
                continue;
            Point pos = findItemGridPosition(item);
            if (pos != null) {
                drawItemAt(g, item, m.startX + pos.x * m.cellSize, m.startY + pos.y * m.cellSize, m.cellSize);
                drawn.add(item);
            }
        }
    }

    /**
     * Draws items located outside the backpack.
     */
    private void drawOutsideItems(Graphics2D g, GridMetrics m) {
        for (Item item : backpack.getOutsideItems()) {
            if (item == draggingItem)
                continue;
            Point pos = backpack.getOutsideItemPosition(item);
            int relX = (pos != null) ? pos.x : 0;
            int relY = (pos != null) ? pos.y : 0;
            drawItemAt(g, item, m.outsideX + relX, m.outsideY + relY + 30, m.cellSize);
        }
    }

    /**
     * Draws the item currently being dragged by the user.
     */
    private void drawDraggingItem(Graphics2D g, GridMetrics m) {
        if (draggingItem != null) {
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.7f));
            drawItemAt(g, draggingItem, (int) (currentMouseX - dragOffsetX), (int) (currentMouseY - dragOffsetY),
                    m.cellSize);
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * Draws an item at the specified coordinates.
     */
    private void drawItemAt(Graphics2D g, Item item, int x, int y, int size) {
        // 1. Draw background (color grid based on shape)
        drawShapeBackground(g, item, x, y, size);

        // 2. Draw item texture
        drawItemTexture(g, item, x, y, size);

        // 3. Draw specific overlays (Mana, HP, etc.)
        drawItemOverlay(g, item, x, y, size);
    }

    /**
     * Draws the item's geometric shape (colored squares).
     */
    private void drawShapeBackground(Graphics2D g, Item item, int x, int y, int size) {
        boolean selected = (item == selectedItem);
        GameView.drawItemShapeBackground(g, item, x, y, size, selected);
    }

    /**
     * Loads and draws the item image, or fallback if not found.
     */
    private void drawItemTexture(Graphics2D g, Item item, int x, int y, int size) {
        GameView.drawItemTexture(g, item, x, y, size);
    }

    /**
     * Delegates specific overlay drawing to each item type.
     */
    private void drawItemOverlay(Graphics2D g, Item item, int x, int y, int size) {
        item.renderOverlay(g, x, y, size);
    }

    /**
     * Draws informational text about available unlock points.
     */
    private void drawUnlockInfo(Graphics2D g, int w, int h) {
        if (backpack.getAvailableUnlocks() > 0) {
            g.setColor(Color.GREEN);
            g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
            g.drawString("Available Points: " + backpack.getAvailableUnlocks(), 20, 40);
        }
    }

    /**
     * Draws the item information box when an item is selected.
     * 
     * @param g Graphics2D context.
     * @param w Screen width.
     * @param h Screen height.
     */
    private void drawInfobox(Graphics2D g, int w, int h) {
        if (selectedItem != null) {
            int x = 20, y = 60;
            drawInfoboxPanel(g, x, y);
            drawInfoboxHeader(g, x, y, selectedItem);
            drawInfoboxShape(g, x, y, selectedItem);
            drawInfoboxStats(g, x, y, selectedItem);
        }
    }

    /**
     * Draws the background panel for the infobox.
     */
    private void drawInfoboxPanel(Graphics2D g, int x, int y) {
        GameView.drawPanel(g, x, y, INFOBOX_WIDTH, INFOBOX_HEIGHT, new Color(0, 0, 0, 220), Color.WHITE, 10);
    }

    /**
     * Draws the name and rarity of the item in the infobox.
     */
    private void drawInfoboxHeader(Graphics2D g, int x, int y, Item item) {
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
        g.drawString(item.name(), x + 15, y + 30);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 12));
        g.drawString(item.rarity().toString(), x + 15, y + 50);
    }

    /**
     * Draws a preview of the item's shape within the infobox.
     */
    private void drawInfoboxShape(Graphics2D g, int x, int y, Item item) {
        Shape s = item.shape();
        int previewCellSize = 25;
        int shapeW = s.width() * previewCellSize;
        int shapeX = x + (INFOBOX_WIDTH - shapeW) / 2;
        int shapeY = y + 70;

        g.setColor(new Color(255, 255, 255, 30));
        for (int i = 0; i < s.width(); i++) {
            for (int j = 0; j < s.height(); j++) {
                if (s.get(i, j)) {
                    g.fillRect(shapeX + i * previewCellSize, shapeY + j * previewCellSize, previewCellSize,
                            previewCellSize);
                    g.setColor(new Color(255, 255, 255, 100));
                    g.drawRect(shapeX + i * previewCellSize, shapeY + j * previewCellSize, previewCellSize,
                            previewCellSize);
                    g.setColor(new Color(255, 255, 255, 30));
                }
            }
        }
    }

    /**
     * Draws the item stats and usage hints in the infobox.
     */
    private void drawInfoboxStats(Graphics2D g, int x, int y, Item item) {
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
        int textY = y + INFOBOX_HEIGHT - 80;

        g.drawString(item.getStatDescription(), x + 10, textY);
        textY += 15;

        if (item.energyCost() > 0) {
            g.drawString("Energy cost: " + item.energyCost(), x + 10, textY);
            textY += 15;
        }

        if (item.manaCost() > 0) {
            g.drawString("Mana cost: " + item.manaCost(), x + 10, textY);
        }

        if (item instanceof Consumable) {
            g.setColor(Color.GREEN);
            g.drawString("Press SPACE to use", x + 10, y + INFOBOX_HEIGHT - 15);
        }
    }

    /**
     * Helper to find the top-left grid position of an item.
     */
    private Point findItemGridPosition(Item item) {
        return GameView.findItemGridPosition(backpack, item);
    }

    @Override
    public void handlePointerEvent(PointerEvent event) {
        float x = event.location().x();
        float y = event.location().y();
        currentMouseX = x;
        currentMouseY = y;

        switch (event.action()) {
            case POINTER_DOWN -> handleDown(x, y);
            case POINTER_UP -> handleUp(x, y);
            default -> {
            }
        }
    }

    /**
     * Main mouse click entry point.
     * Tries to interact with the grid, then the outside zone, otherwise deselects.
     */
    private void handleDown(float x, float y) {
        if (lastMetrics == null)
            return;

        // 1. Try to handle a click in the grid
        if (handleGridInteraction(x, y, lastMetrics)) {
            return;
        }

        // 2. Otherwise, try to handle a click on outside items
        if (handleOutsideInteraction(x, y, lastMetrics)) {
            return;
        }

        // 3. If clicking on empty space, deselect everything
        selectedItem = null;
    }

    /**
     * Handles logic related to the backpack grid (unlocking or item selection).
     * 
     * @return true if an action was performed in the grid.
     */
    private boolean handleGridInteraction(float x, float y, GridMetrics m) {
        int gx = (int) ((x - m.startX) / m.cellSize);
        int gy = (int) ((y - m.startY) / m.cellSize);

        if (gx >= 0 && gx < backpack.getWidth() && gy >= 0 && gy < backpack.getHeight()) {
            return tryInteractWithCell(gx, gy, x, y, m);
        }
        return false;
    }

    /**
     * Attempts to interact with a specific grid cell (unlock or select item).
     * 
     * @return true if an interaction occurred.
     */
    private boolean tryInteractWithCell(int gx, int gy, float x, float y, GridMetrics m) {
        if (!backpack.isActive(gx, gy)) {
            backpack.unlock(gx, gy);
            return true;
        }

        Item item = backpack.getItemAt(gx, gy);
        if (item != null) {
            processGridItem(item, x, y, m);
            return true;
        }
        return false;
    }

    /**
     * Handles selection and dragging of an item in the grid.
     * Curses are selectable but not movable.
     */
    private void processGridItem(Item item, float mouseX, float mouseY, GridMetrics m) {
        // All items can be selected to see their stats
        selectedItem = item;

        // Only draggable items can be dragged
        if (item.isDraggable()) {
            Point anchor = findItemGridPosition(item);
            float offsetX = mouseX - (m.startX + anchor.x * m.cellSize);
            float offsetY = mouseY - (m.startY + anchor.y * m.cellSize);

            startDragging(item, anchor.x, anchor.y, false, offsetX, offsetY);
        }
    }

    /**
     * Handles click logic on items outside the bag (loot).
     * 
     * @return true if an outside item was clicked.
     */
    private boolean handleOutsideInteraction(float x, float y, GridMetrics m) {
        for (Item item : backpack.getOutsideItems()) {
            if (tryInteractWithOutsideItem(item, x, y, m)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to interact with a specific item outside the grid.
     * 
     * @return true if the item was clicked.
     */
    private boolean tryInteractWithOutsideItem(Item item, float x, float y, GridMetrics m) {
        Point pos = backpack.getOutsideItemPosition(item);
        int itemScreenX = m.outsideX + pos.x;
        int itemScreenY = m.outsideY + pos.y + 30; // The +30 is a UI padding
        int sw = item.shape().width() * m.cellSize;
        int sh = item.shape().height() * m.cellSize;

        if (x >= itemScreenX && x < itemScreenX + sw &&
                y >= itemScreenY && y < itemScreenY + sh) {

            originalOutsidePos = new Point(pos.x, pos.y);
            startDragging(item, -1, -1, true, x - itemScreenX, y - itemScreenY);
            return true;
        }
        return false;
    }

    /**
     * Initiates the dragging state for an item.
     */
    private void startDragging(Item item, int gx, int gy, boolean outside, float offX, float offY) {
        draggingItem = item;
        selectedItem = item;
        originalGridX = gx;
        originalGridY = gy;
        fromOutside = outside;
        dragOffsetX = offX;
        dragOffsetY = offY;
    }

    /**
     * Handles the pointer release event to drop the item.
     */
    private void handleUp(float x, float y) {
        if (draggingItem != null) {
            dropItem(x, y);
            draggingItem = null;
        }
    }

    /**
     * Logic for dropping an item into a destination zone.
     */
    private void dropItem(float mx, float my) {
        GridMetrics m = lastMetrics;
        if (m == null)
            return;

        // Complete removal before attempt to place (avoids duplication)
        backpack.completelyRemoveItem(draggingItem);

        if (tryDropInGrid(mx, my, m))
            return;
        if (tryDropOutside(mx, my, m))
            return;

        // 3. EN DEHORS DES DEUX ZONES -> Retour à l'envoyeur
        restoreOriginalPosition();
    }

    /**
     * Attempts to drop the item into the backpack grid.
     * 
     * @return true if successful.
     */
    private boolean tryDropInGrid(float mx, float my, GridMetrics m) {
        float effX = mx - dragOffsetX;
        float effY = my - dragOffsetY;
        int gx = Math.round((effX - m.startX) / m.cellSize);
        int gy = Math.round((effY - m.startY) / m.cellSize);

        boolean insideGridArea = (mx > m.startX && mx < m.startX + m.gridWidth && my > m.startY
                && my < m.startY + m.gridHeight);

        if (insideGridArea) {
            return backpack.add(draggingItem, gx, gy);
        }
        return false;
    }

    /**
     * Attempts to drop the item into the outside overflow zone.
     * 
     * @return true if successful.
     */
    private boolean tryDropOutside(float mx, float my, GridMetrics m) {
        boolean targetIsOutsideZone = (mx >= m.outsideX && mx <= m.outsideX + m.outsideW &&
                my >= m.outsideY && my <= m.outsideY + m.outsideH);

        if (targetIsOutsideZone) {
            backpack.placeOutside(draggingItem);
            float effX = mx - dragOffsetX;
            float effY = my - dragOffsetY;
            int relX = (int) (effX - m.outsideX);
            int relY = (int) (effY - m.outsideY - 30);
            backpack.setOutsideItemPosition(draggingItem, relX, relY);
            return true;
        }
        return false;
    }

    /**
     * Restores an item to its original position if a drop fails.
     */
    private void restoreOriginalPosition() {
        if (fromOutside) {
            backpack.placeOutside(draggingItem);
            backpack.setOutsideItemPosition(draggingItem, originalOutsidePos.x, originalOutsidePos.y);
        } else {
            if (!backpack.add(draggingItem, originalGridX, originalGridY)) {
                // Safety check: if we cannot restore to original grid position (should not
                // happen if logic is correct),
                // do not delete the item. Place it outside instead.
                System.err.println("Warning: Failed to restore item to original grid position (" + originalGridX + ","
                        + originalGridY + "). Placing outside.");
                backpack.placeOutside(draggingItem);
            }
        }
    }

    @Override
    public void handleKeyBoardEvent(KeyboardEvent event) {
        if (event.action() == KeyboardEvent.Action.KEY_PRESSED) {
            switch (event.key()) {
                case ESCAPE -> done = true;
                case R -> handleRotation();
                case SPACE, U -> handleUsage();
                default -> {
                }
            }
        }
    }

    /**
     * Handles usage of consumable items when the appropriate key is pressed.
     */
    private void handleUsage() {
        if (selectedItem instanceof Consumable consumable) {
            consumable.applyEffect(hero);
            backpack.completelyRemoveItem(consumable);
            selectedItem = null;
            draggingItem = null;
        }
    }

    /**
     * Handles rotation of the selected item if allowed.
     */
    private void handleRotation() {
        if (selectedItem == null || !selectedItem.isRotatable()) {
            return;
        }

        Item newItem = rotateSelected();
        if (newItem != null) {
            updateSelectionAfterRotation(newItem);
        }
    }

    /**
     * Rotates the selected item based on its current location (grid or outside).
     * 
     * @return The new rotated item instance.
     */
    private Item rotateSelected() {
        if (backpack.getItems().contains(selectedItem)) {
            return backpack.rotate(selectedItem);
        } else {
            return backpack.rotateOutsideItem(selectedItem);
        }
    }

    /**
     * Updates selection references after a successful rotation.
     */
    private void updateSelectionAfterRotation(Item newItem) {
        if (draggingItem == selectedItem) {
            draggingItem = newItem;
            syncOriginalPosition(newItem);
        }
        selectedItem = newItem;
    }

    /**
     * Synchronizes the original position data with the newly rotated item.
     */
    private void syncOriginalPosition(Item newItem) {
        if (backpack.getItems().contains(newItem)) {
            Point pos = findItemGridPosition(newItem);
            if (pos != null) {
                originalGridX = pos.x;
                originalGridY = pos.y;
                fromOutside = false;
            }
        } else {
            Point pos = backpack.getOutsideItemPosition(newItem);
            if (pos != null) {
                originalOutsidePos = new Point(pos.x, pos.y);
                fromOutside = true;
            }
        }
    }

    /**
     * Logic update called every frame.
     */
    @Override
    public void updateLogic() {
    }

    /**
     * Returns true if the view is finished and should transition.
     */
    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the next view to display.
     */
    @Override
    public GameView nextView() {
        return parentView;
    }
}