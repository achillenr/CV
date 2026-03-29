package item;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BackPack {
    private static final int MAX_WIDTH = 9;
    private static final int MAX_HEIGHT = 5;

    public static int getMaxWidth() {
        return MAX_WIDTH;
    }

    public static int getMaxHeight() {
        return MAX_HEIGHT;
    }

    // Instead of dynamic width/height, we report MAX dimensions to the view,
    // but we track active cells.
    private int width = MAX_WIDTH;
    private int height = MAX_HEIGHT;
    private int availableUnlocks = 0; // Available unlock points

    private Item[][] grid;
    private boolean[][] activeCells; // New mask
    private final List<Item> items;
    private final List<Item> outsideItems;
    private final Map<Item, Point> outsidePositions;
    private boolean initialized = false;

    public BackPack() {
        this(3, 3);
    }

    public BackPack(int initialW, int initialH) {
        this.grid = new Item[MAX_WIDTH][MAX_HEIGHT];
        this.activeCells = new boolean[MAX_WIDTH][MAX_HEIGHT];

        // Initialize active cells in the center
        int startX = (MAX_WIDTH - initialW) / 2;
        int startY = (MAX_HEIGHT - initialH) / 2;

        for (int x = startX; x < startX + initialW; x++) {
            for (int y = startY; y < startY + initialH; y++) {
                if (x >= 0 && x < MAX_WIDTH && y >= 0 && y < MAX_HEIGHT) {
                    activeCells[x][y] = true;
                }
            }
        }

        this.items = new ArrayList<>();
        this.outsideItems = new ArrayList<>();
        this.outsidePositions = new HashMap<>();
    }

    /**
     * Returns the width of the backpack grid.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the backpack grid.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Attempts to add an item to the backpack.
     * 1. Tries to stack if it's gold.
     * 2. Searches for a free spot in the grid.
     * 3. If it fails, places the item outside (overflow).
     */
    public boolean add(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (tryStackGold(item))
            return true;

        if (tryPlaceInGrid(item))
            return true;

        handleItemOverflow(item);
        return true;
    }

    // ---------------------------------------------------------
    // PRIVATE HELPER METHODS
    // ---------------------------------------------------------

    /**
     * Checks if the item is gold and if there is already a gold stack to merge
     * with.
     */
    private boolean tryStackGold(Item newItem) {
        Objects.requireNonNull(newItem, "newItem cannot be null");
        switch (newItem) {
            case Gold newGold -> {
                Gold existingGold = findExistingGold();
                if (existingGold != null) {
                    mergeGold(existingGold, newGold);
                    return true;
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Searches for an existing Gold item in the backpack items list.
     * 
     * @return The existing Gold item, or null if not found.
     */
    private Gold findExistingGold() {
        for (Item it : items) {
            switch (it) {
                case Gold g -> {
                    return g;
                }
                default -> {
                }
            }
        }
        return null;
    }

    /**
     * Merges two stacks of gold:
     * Creates a new combined Gold record, removes the old one, and places the new
     * one.
     */
    private void mergeGold(Gold existingGold, Gold newGold) {
        Objects.requireNonNull(existingGold, "existingGold cannot be null");
        Objects.requireNonNull(newGold, "newGold cannot be null");
        Gold mergedGold = createMergedGold(existingGold, newGold);
        replaceGoldItem(existingGold, mergedGold);
    }

    /**
     * Creates a new Gold object with the combined amount of two gold stacks.
     * 
     * @param existingGold The current gold stack in the backpack.
     * @param newGold      The new gold stack being added.
     * @return A new Gold item representing the merged stack.
     */
    private Gold createMergedGold(Gold existingGold, Gold newGold) {
        Objects.requireNonNull(existingGold, "existingGold cannot be null");
        Objects.requireNonNull(newGold, "newGold cannot be null");
        int totalAmount = existingGold.amount() + newGold.amount();
        return new Gold(
                existingGold.id(),
                existingGold.name(),
                totalAmount,
                existingGold.isSellable(),
                existingGold.rarity(),
                existingGold.shape());
    }

    /**
     * Replaces an old gold item with a new one in the grid and item list.
     * 
     * @param oldGold The gold item to remove.
     * @param newGold The gold item to add in its place.
     */
    private void replaceGoldItem(Gold oldGold, Gold newGold) {
        Objects.requireNonNull(oldGold, "oldGold cannot be null");
        Objects.requireNonNull(newGold, "newGold cannot be null");
        Point pos = findItemPosition(oldGold);
        if (pos != null) {
            completelyRemoveItem(oldGold);
            place(newGold, pos.x, pos.y);
        }
    }

    /**
     * Iterates through the grid to try placing the object in the first available
     * space.
     */
    private boolean tryPlaceInGrid(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (canFit(item, x, y)) {
                    place(item, x, y);
                    removeFromOutsideLists(item);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles the case where the item does not fit: places it in the "outside" zone
     * if necessary.
     */
    private void handleItemOverflow(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!outsideItems.contains(item)) {
            placeOutside(item);
        }
    }

    /**
     * Helper to clean up outside item lists.
     */
    private void removeFromOutsideLists(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        outsideItems.remove(item);
        outsidePositions.remove(item);
    }

    /**
     * Finds the coordinates (top-left corner) of an item in the grid.
     * Returns null if the item is not in the grid.
     */
    private Point findItemPosition(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != null && grid[x][y].id() == item.id()) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }

    /**
     * Tries to add an item at a specific position.
     * 
     * @param item The item to add.
     * @param x    Top-left x coordinate.
     * @param y    Top-left y coordinate.
     * @return true if added, false if invalid or occupied.
     */
    public boolean add(Item item, int x, int y) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!canFit(item, x, y)) {
            return false;
        }

        removeInternal(item); // remove from old position
        place(item, x, y); // add to grid + items
        outsideItems.remove(item); // in case it was outside
        outsidePositions.remove(item);
        return true;
    }

    /**
     * Removes an item from the grid.
     * If successful, the item is moved to the outside overflow list.
     * 
     * @param item The item to remove.
     * @return true if the item was found and removed, false otherwise.
     */
    public boolean remove(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        Point pos = findItemPosition(item);
        boolean inBackpack = (pos != null); // Check if item is in the grid
        if (!inBackpack) {
            return false;
        }

        // Remove from items list and grid
        items.remove(item);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == item) {
                    grid[x][y] = null;
                }
            }
        }

        if (!outsideItems.contains(item)) {
            outsideItems.add(item); // removed from bag -> outside
            outsidePositions.putIfAbsent(item, new Point(0, 0));
        }
        return true;
    }

    /**
     * Completely removes an item from the backpack, including the outside items
     * list.
     * 
     * @param item The item to remove.
     * @return true if the item was removed, false otherwise.
     */
    public boolean completelyRemoveItem(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        boolean inGrid = removeFromGrid(item);
        boolean removedFromOutside = removeOutsideItemById(item.id());

        return inGrid || removedFromOutside;
    }

    private boolean removeOutsideItemById(int id) {
        Item toRemove = null;
        for (Item it : outsideItems) {
            if (it.id() == id) {
                toRemove = it;
                break;
            }
        }
        if (toRemove != null) {
            outsideItems.remove(toRemove);
            outsidePositions.remove(toRemove);
            return true;
        }
        return false;
    }

    private boolean removeFromGrid(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        boolean found = false;
        // Aggressive sweep: remove ALL occurrences of this item ID from the grid
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != null && grid[x][y].id() == item.id()) {
                    grid[x][y] = null;
                    found = true;
                }
            }
        }

        // Also ensure removal from items list
        // We use removeIf to handle potential stale references with same ID
        items.removeIf(it -> it.id() == item.id());

        return found;
    }

    public void expand(int extraWidth, int extraHeight) {
        validateExpansion(extraWidth, extraHeight);
        if (extraWidth == 0 && extraHeight == 0)
            return;

        int newWidth = width + extraWidth;
        int newHeight = height + extraHeight;
        Item[][] newGrid = createExpandedGrid(newWidth, newHeight);

        this.width = newWidth;
        this.height = newHeight;
        this.grid = newGrid;
    }

    private void validateExpansion(int w, int h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException("Cannot expand by negative amount");
        }
    }

    /**
     * Creates a new grid with larger dimensions, copying existing items.
     * 
     * @param newWidth  The new width.
     * @param newHeight The new height.
     * @return The new, larger grid.
     */
    private Item[][] createExpandedGrid(int newWidth, int newHeight) {
        Item[][] newGrid = new Item[newWidth][newHeight];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                newGrid[x][y] = grid[x][y];
            }
        }
        return newGrid;
    }

    /**
     * Checks if the item fits at the given coordinates.
     * Relaxed check: checks if each OCCUPIED cell of the shape is within bounds and
     * valid.
     * Empty cells of the shape can be out of bounds.
     */
    public boolean canFit(Item item, int x, int y) {
        Objects.requireNonNull(item, "item cannot be null");
        Shape s = item.shape();
        for (int i = 0; i < s.width(); i++) {
            for (int j = 0; j < s.height(); j++) {
                if (s.get(i, j) && !isCellFree(x + i, y + j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a specific cell coordinates are valid for placement.
     * 
     * @param gx Grid x coordinate.
     * @param gy Grid y coordinate.
     * @return true if the cell is within bounds, empty, and unlocked.
     */
    private boolean isCellFree(int gx, int gy) {
        if (gx < 0 || gx >= width || gy < 0 || gy >= height)
            return false;
        if (grid[gx][gy] != null)
            return false;
        return activeCells[gx][gy];
    }

    /**
     * Places the item in the grid at the specified coordinates.
     * Assumes checks have already been performed.
     * 
     * @param item The item to place.
     * @param x    The grid x coordinate.
     * @param y    The grid y coordinate.
     */
    private void place(Item item, int x, int y) {
        Objects.requireNonNull(item, "item cannot be null");
        Shape s = item.shape();
        int sw = s.width();
        int sh = s.height();

        for (int i = 0; i < sw; i++) {
            for (int j = 0; j < sh; j++) {
                if (s.get(i, j)) {
                    grid[x + i][y + j] = item;
                }
            }
        }
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    // Add this private method in BackPack.java
    /**
     * Adds an item to the "outside" overflow zone.
     * Calculates a visual position for the item.
     * 
     * @param item The item to place outside.
     */
    public void placeOutside(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!outsideItems.contains(item)) {
            outsideItems.add(item);
        }

        int index = outsideItems.indexOf(item);
        int spacing = 60; // Horizontal space between each item
        int startY = 0; // Y position relative to outside zone
        int startX = 10; // Small left padding

        // Calculate horizontal line position
        int itemsPerRow = 8; // More items per row
        int row = index / itemsPerRow;
        int col = index % itemsPerRow;

        int x = startX + (col * spacing); // Horizontal offset
        int y = startY + (row * 60); // Vertical offset if multiple rows

        outsidePositions.put(item, new Point(x, y));
    }

    /**
     * Retrieves the item at the specified grid coordinates.
     * 
     * @param x Grid x coordinate.
     * @param y Grid y coordinate.
     * @return The item at active cell (x,y), or null if empty/invalid.
     */
    public Item getItemAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return null;
        return grid[x][y];
    }

    /**
     * Returns a copy of the list of items currently in the backpack (grid).
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Rotates an item currently inside the backpack grid.
     * Checks validity before applying rotation.
     * 
     * @param item The item to rotate.
     * @return The new rotated item instance, or null if rotation failed
     *         (collision).
     */
    public Item rotate(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!item.isRotatable()) {
            return null;
        }
        if (!items.contains(item))
            return null;

        Point visualPos = findBoundingBoxTopLeft(item);
        if (visualPos == null)
            return null;

        Point origAnchor = calculateAnchor(item.shape(), visualPos);
        Item rotated = item.withShape(item.shape().rotate());
        Point newAnchor = calculateAnchor(rotated.shape(), visualPos);

        return trySwapRotated(item, rotated, origAnchor, newAnchor);
    }

    /**
     * Calculates the top-left anchor point for a shape.
     * 
     * @param shape     The item shape.
     * @param visualPos The visual top-left position.
     * @return The logic anchor point.
     */
    private Point calculateAnchor(Shape shape, Point visualPos) {
        Objects.requireNonNull(shape, "shape cannot be null");
        Objects.requireNonNull(visualPos, "visualPos cannot be null");
        Point offset = getShapeBoundingBoxOffset(shape);
        return new Point(visualPos.x - offset.x, visualPos.y - offset.y);
    }

    /**
     * Finds the top-left bounding box corner of an item in the grid.
     * 
     * @param item The item to locate.
     * @return The top-left Point, or null if not found.
     */
    public Point findBoundingBoxTopLeft(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        Point activePos = findItemPosition(item);
        if (activePos == null) { // Item not in grid
            return null;
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        boolean found = false;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == item) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    found = true;
                }
            }
        }
        return found ? new Point(minX, minY) : null;
    }

    /**
     * Calculates the offset of the first occupied cell in the shape bounding box.
     * 
     * @param shape The shape to analyze.
     * @return The offset as a Point.
     */
    private Point getShapeBoundingBoxOffset(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;

        for (int i = 0; i < shape.width(); i++) {
            for (int j = 0; j < shape.height(); j++) {
                if (shape.get(i, j)) {
                    minX = Math.min(minX, i);
                    minY = Math.min(minY, j);
                }
            }
        }
        return new Point(minX, minY);
    }

    /**
     * Attempts to replace an original item with its rotated version.
     * 
     * @param original The original item.
     * @param rotated  The rotated item.
     * @param oldPos   The position of the original item.
     * @param newPos   The target position for the rotated item.
     * @return The rotated Item if successful, null otherwise.
     */
    private Item trySwapRotated(Item original, Item rotated, Point oldPos, Point newPos) {
        Objects.requireNonNull(original, "original item cannot be null");
        Objects.requireNonNull(rotated, "rotated item cannot be null");
        Objects.requireNonNull(oldPos, "oldPos cannot be null");
        Objects.requireNonNull(newPos, "newPos cannot be null");

        removeInternal(original);

        if (canFit(rotated, newPos.x, newPos.y)) {
            place(rotated, newPos.x, newPos.y);
            return rotated;
        } else {
            // Failure: put the original item back in its exact original place
            place(original, oldPos.x, oldPos.y);
            return null;
        }
    }

    /**
     * Gets visual offset of the shape's first active pixel.
     * 
     * @param shape The shape.
     * @return The visual offset Point.
     */
    private Point getShapeVisualOffset(Shape shape) {
        for (int i = 0; i < shape.width(); i++) {
            for (int j = 0; j < shape.height(); j++) {
                if (shape.get(i, j))
                    return new Point(i, j); // First pixel found
            }
        }
        return new Point(0, 0);
    }

    /**
     * Tries to rotate an item that is currently outside the grid.
     * 
     * @param item The item to rotate.
     * @return The new rotated item if successful, null otherwise.
     */
    public Item rotateOutsideItem(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!item.isRotatable()) {
            return null;
        }
        if (!outsideItems.contains(item))
            return null;

        Item rotated = item.withShape(item.shape().rotate());
        updateOutsidePositionForRotation(item, rotated);

        outsideItems.remove(item);
        outsideItems.add(rotated);

        return rotated;
    }

    /**
     * Updates the map tracking positions of outside items after a rotation.
     * 
     * @param oldItem The previous item record.
     * @param newItem The new rotated item record.
     */
    private void updateOutsidePositionForRotation(Item oldItem, Item newItem) {
        Objects.requireNonNull(oldItem, "oldItem cannot be null");
        Objects.requireNonNull(newItem, "newItem cannot be null");
        Point pos = outsidePositions.remove(oldItem);
        if (pos == null) {
            outsidePositions.put(newItem, new Point(0, 0));
            return;
        }

        Point oldOffset = getShapeVisualOffset(oldItem.shape());
        Point newOffset = getShapeVisualOffset(newItem.shape());

        int nx = pos.x + oldOffset.x - newOffset.x;
        int ny = pos.y + oldOffset.y - newOffset.y;
        outsidePositions.put(newItem, new Point(nx, ny));
    }

    /**
     * Initializes the backpack with default starter items.
     * Only runs once.
     */
    public void init_backpack() {
        if (initialized)
            return;

        addInitialShield();
        addInitialSword();
        addInitialKey();

        initialized = true;
    }

    private void addInitialShield() {
        boolean[][] s = { { true } };
        add(new Shield(ItemFactory.generateId(), "Worn Shield", 5, 1, true, Rarity.COMMON, new Shape(s),
                java.util.Map.of()));
    }

    private void addInitialSword() {
        boolean[][] s2 = { { true, true }, { true } };
        add(new MeleeWeapon(ItemFactory.generateId(), "Wooden Sword", 5, 1, true, Rarity.COMMON, new Shape(s2),
                java.util.Map.of()));
    }

    private void addInitialKey() {
        boolean[][] sKey = { { true } };
        add(new Key(ItemFactory.generateId(), "Golden Key", true, Rarity.KEY, new Shape(sKey)));
    }

    /**
     * Gets the visual position of an item currently outside the grid.
     * 
     * @param item The outside item.
     * @return The position Point, or null if not found.
     */
    public Point getOutsideItemPosition(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        return outsidePositions.get(item);
    }

    /**
     * Sets the visual position for an outside item.
     * 
     * @param item The outside item.
     * @param x    The x coordinate.
     * @param y    The y coordinate.
     */
    public void setOutsideItemPosition(Item item, int x, int y) {
        Objects.requireNonNull(item, "item cannot be null");
        outsidePositions.put(item, new Point(x, y));
    }

    /**
     * Returns a copy of the list of items outside the grid.
     * 
     * @return List of outside items.
     */
    public List<Item> getOutsideItems() {
        return new ArrayList<>(outsideItems);
    }

    /**
     * Internal helper to remove item from collections without side effects like
     * moving to outside.
     * 
     * @param item The item to remove.
     */
    private void removeInternal(Item item) {
        Objects.requireNonNull(item, "item cannot be null");
        items.remove(item);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == item) {
                    grid[x][y] = null;
                }
            }
        }
    }

    /**
     * Places a Curse item at a specific location, clearing other items if
     * necessary.
     * 
     * @param curse The curse item.
     * @param gx    Grid x coordinate.
     * @param gy    Grid y coordinate.
     * @return true if placement successful.
     */
    public boolean placeCurse(Item curse, int gx, int gy) {
        Objects.requireNonNull(curse, "curse cannot be null");

        if (!canPlaceCurse(curse.shape(), gx, gy)) {
            return false;
        }

        clearAreaForCurse(curse.shape(), gx, gy);
        place(curse, gx, gy);
        return true;
    }

    /**
     * Checks if a curse can be placed (target cells must be valid and unlocked).
     */
    private boolean canPlaceCurse(Shape shape, int gx, int gy) {
        for (int dx = 0; dx < shape.width(); dx++) {
            for (int dy = 0; dy < shape.height(); dy++) {
                if (shape.get(dx, dy)) {
                    if (!isValidAndActive(gx + dx, gy + dy)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Clears any existing items in the area where a curse is landing.
     */
    private void clearAreaForCurse(Shape shape, int gx, int gy) {
        for (int dx = 0; dx < shape.width(); dx++) {
            for (int dy = 0; dy < shape.height(); dy++) {
                if (shape.get(dx, dy)) {
                    Item existing = getItemAt(gx + dx, gy + dy);
                    if (existing != null) {
                        completelyRemoveItem(existing);
                    }
                }
            }
        }
    }

    /**
     * Removes all Curse items from the backpack.
     */
    public void removeAllCurses() {
        List<Item> toRemove = new ArrayList<>();
        for (Item item : items) {
            switch (item) {
                case Curse c -> toRemove.add(item);
                default -> {
                }
            }
        }
        for (Item item : toRemove) {
            completelyRemoveItem(item);
        }
    }

    /**
     * Checks if a cell is unlocked and valid.
     * 
     * @param x Grid x.
     * @param y Grid y.
     * @return true if valid and active.
     */
    public boolean isActive(int x, int y) {
        if (x < 0 || x >= MAX_WIDTH || y < 0 || y >= MAX_HEIGHT)
            return false;
        return activeCells[x][y];
    }

    /**
     * Identifies all cells that can currently be unlocked.
     * 
     * @return List of Points representing unlockable cells.
     */
    public List<Point> getUnlockableCells() {
        if (availableUnlocks <= 0)
            return new ArrayList<>();

        List<Point> unlockable = new ArrayList<>();
        for (int x = 0; x < MAX_WIDTH; x++) {
            for (int y = 0; y < MAX_HEIGHT; y++) {
                if (!activeCells[x][y] && hasActiveNeighbor(x, y)) {
                    unlockable.add(new Point(x, y));
                }
            }
        }
        return unlockable;
    }

    /**
     * Checks if a cell has at least one active neighbor.
     * 
     * @param x Cell x coordinate.
     * @param y Cell y coordinate.
     * @return true if an active neighbor exists.
     */
    private boolean hasActiveNeighbor(int x, int y) {
        int[][] dirs = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (isValidAndActive(nx, ny))
                return true;
        }
        return false;
    }

    /**
     * Checks if coordinates are valid and the cell is active.
     */
    private boolean isValidAndActive(int x, int y) {
        return x >= 0 && x < MAX_WIDTH && y >= 0 && y < MAX_HEIGHT && activeCells[x][y];
    }

    /**
     * Unlocks a cell if possible (points available, adjacent).
     * 
     * @param x Cell x coordinate.
     * @param y Cell y coordinate.
     * @return true if successfully unlocked.
     */
    public boolean unlock(int x, int y) {
        if (x < 0 || x >= MAX_WIDTH || y < 0 || y >= MAX_HEIGHT)
            return false;
        if (activeCells[x][y])
            return false; // Already active

        if (availableUnlocks <= 0)
            return false; // Not enough points

        // Verify it is unlockable (adjacent to active)
        List<Point> candidates = getUnlockableCells();
        if (candidates.contains(new Point(x, y))) {
            activeCells[x][y] = true;
            availableUnlocks--; // Consume a point
            return true;
        }
        return false;
    }

    /**
     * Checks if expansion is possible (available points and possible extensions).
     */
    public boolean canExpand() {
        return availableUnlocks > 0 && !getUnlockableCells().isEmpty();
    }

    /**
     * Adds skill points available for unlocking grid cells.
     * 
     * @param amount The amount of points to add.
     */
    public void addUnlockPoints(int amount) {
        if (amount > 0) {
            this.availableUnlocks += amount;
        }
    }

    /**
     * Gets the number of available unlock points.
     */
    public int getAvailableUnlocks() {
        return availableUnlocks;
    }

    /**
     * Clears all items currently in the outside overflow zone.
     */
    public void clearOutsideItems() {
        outsideItems.clear();
        outsidePositions.clear();
    }

    /**
     * Calculates the total value of all items in the backpack (grid + outside).
     */
    public int getTotalItemValue() {
        int total = 0;
        for (Item item : items) {
            total += item.getValue();
        }
        for (Item item : outsideItems) {
            total += item.getValue();
        }
        return total;
    }

    /**
     * Finds all ManaStone items connected to the given start item.
     * Starts BFS/DFS from the item's grid position.
     * 
     * @param startItem The item initiating the search (e.g., a weapon).
     * @return List of connected ManaStone items.
     */
    public List<ManaStone> getConnectedManaStones(Item startItem) {
        Objects.requireNonNull(startItem, "startItem cannot be null");
        List<ManaStone> stones = new ArrayList<>();
        Point startPos = findItemPosition(startItem);
        if (startPos == null)
            return stones;

        List<Point> visited = new ArrayList<>();
        Shape shape = startItem.shape();

        for (int i = 0; i < shape.width(); i++) {
            for (int j = 0; j < shape.height(); j++) {
                if (shape.get(i, j)) {
                    findConnectedManaRecursively(startPos.x + i, startPos.y + j, stones, visited);
                }
            }
        }
        return stones;
    }

    /**
     * Recursive helper to explore grid for connected mana stones.
     */
    private void findConnectedManaRecursively(int x, int y, List<ManaStone> stones, List<Point> visited) {
        Objects.requireNonNull(stones, "stones cannot be null");
        Objects.requireNonNull(visited, "visited cannot be null");
        Point current = new Point(x, y);
        if (visited.contains(current))
            return;
        visited.add(current);

        int[][] directions = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
        for (int[] dir : directions) {
            checkNeighborForMana(x + dir[0], y + dir[1], stones, visited);
        }
    }

    /**
     * Checks a neighbor cell for a ManaStone.
     * If found and new, adds it and recurses.
     */
    private void checkNeighborForMana(int nx, int ny, List<ManaStone> stones, List<Point> visited) {
        Objects.requireNonNull(stones, "stones cannot be null");
        Objects.requireNonNull(visited, "visited cannot be null");
        if (nx < 0 || nx >= getWidth() || ny < 0 || ny >= getHeight())
            return;

        Item neighbor = grid[nx][ny];
        if (neighbor != null && neighbor.isManaStone()) {
            ManaStone stone = neighbor.asManaStone();
            if (!stones.contains(stone)) {
                stones.add(stone);
                findConnectedManaRecursively(nx, ny, stones, visited);
            }
        }
    }

    /**
     * Consumes mana from connected mana stones to power an item/weapon.
     * 
     * @param weapon The weapon requesting mana.
     * @param amount The required mana amount.
     * @return true if sufficient mana was available and consumed, false otherwise.
     */
    public boolean consumeConnectedMana(Item weapon, int amount) {
        Objects.requireNonNull(weapon, "weapon cannot be null");
        java.util.List<ManaStone> stones = getConnectedManaStones(weapon);

        if (calculateTotalMana(stones) < amount) {
            return false;
        }

        performManaConsumption(stones, amount);
        return true;
    }

    /**
     * Calculates total mana available in a list of stones.
     */
    private int calculateTotalMana(List<ManaStone> stones) {
        Objects.requireNonNull(stones, "stones cannot be null");
        int total = 0;
        for (ManaStone stone : stones) {
            total += stone.currentMana();
        }
        return total;
    }

    /**
     * Deducts the specified amount of mana from the provided stones.
     * Distributes the cost across stones locally.
     */
    private void performManaConsumption(List<ManaStone> stones, int amount) {
        Objects.requireNonNull(stones, "stones cannot be null");
        int toConsume = amount;
        for (ManaStone stone : stones) {
            int available = stone.currentMana();
            if (available > 0) {
                int taking = Math.min(toConsume, available);
                stone.consume(taking);
                toConsume -= taking;
                if (toConsume <= 0)
                    break;
            }
        }
    }

    /**
     * Refills all mana stones in the backpack to their maximum capacity.
     */
    public void refillManaStones() {
        for (Item item : getItems()) {
            switch (item) {
                case ManaStone stone -> stone.refill();
                default -> {
                } // Ignore all other item types
            }
        }
    }

    /**
     * Gets the total current mana available to a specific connected weapon.
     * 
     * @param weapon The weapon item.
     * @return Total mana points available.
     */
    public int getConnectedMana(Item weapon) {
        Objects.requireNonNull(weapon, "weapon cannot be null");
        int total = 0;
        for (ManaStone stone : getConnectedManaStones(weapon)) {
            total += stone.currentMana();
        }
        return total;
    }

}
