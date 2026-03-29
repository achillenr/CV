package item;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a merchant that manages a shop of collectible items.
 * Handles item generation, price calculation based on rarity, and the buy/sell
 * transactions with the hero's backpack.
 */
public class Merchant {

    private final List<Item> shop = new ArrayList<>();
    private final List<Integer> prices = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Constructs a new merchant and initializes the shop with a default set of
     * random items.
     */
    public Merchant() {
        initShop();
    }

    /**
     * Populates the shop with an initial set of 7 random items.
     * Skips gold items as the merchant sells equipment, not currency pouches.
     */
    private void initShop() {
        int itemsToGenerate = 7;
        while (itemsToGenerate > 0) {
            Item randomItem = ItemFactory.createRandomTreasure();
            switch (randomItem) {
                case Gold g -> {
                    // Merchant doesn't sell gold pouches
                }
                default -> {
                    addItemToShop(randomItem);
                    itemsToGenerate--;
                }
            }
        }
    }

    /**
     * Adds a specific item to the shop's inventory and calculates its listing
     * price.
     * 
     * @param item The item to add.
     */
    private void addItemToShop(Item item) {
        shop.add(item);
        prices.add(calculateBasePrice(item));
    }

    /**
     * Calculates the base price of an item using its rarity.
     * The price calculation is deterministic based on the item instance's hash code
     * to ensure consistency.
     * 
     * @param item The item to value.
     * @return The calculated price in gold.
     */
    public int calculateBasePrice(Item item) {
        // Use a deterministic seed based on item properties so the price is constant
        // for the same item instance
        long seed = item.hashCode();
        Random deterministicRandom = new Random(seed);

        return switch (item.rarity()) {
            case COMMON -> 10 + deterministicRandom.nextInt(15);
            case RARE -> 45 + deterministicRandom.nextInt(35);
            case EPIC -> 130 + deterministicRandom.nextInt(70);
            case LEGENDARY -> 380 + deterministicRandom.nextInt(120);
            default -> 25;
        };
    }

    /**
     * Returns a safe copy of the shop's current inventory.
     * 
     * @return A List of items.
     */
    public List<Item> getShop() {
        return new ArrayList<>(shop);
    }

    /**
     * Retrieves the listed price for an item at a specific shop index.
     * 
     * @param index The index in the shop inventory.
     * @return The price in gold, or 0 if the index is out of bounds.
     */
    public int getPrice(int index) {
        if (index < 0 || index >= prices.size())
            return 0;
        return prices.get(index);
    }

    /**
     * Attempts to complete a purchase from the shop.
     * 
     * @param backpack The hero's backpack to add the item to.
     * @param index    The index of the item in the shop.
     * @param row      Desired grid row in the backpack.
     * @param col      Desired grid column in the backpack.
     * @return true if the purchase was successful and the item was fitted into the
     *         backpack.
     */
    public boolean buy(BackPack backpack, int index, int row, int col) {
        if (index < 0 || index >= shop.size())
            return false;

        Item item = shop.get(index);
        boolean added = false;
        if (backpack.canFit(item, row, col)) {
            backpack.add(item, row, col);
            added = true;
        } else {
            added = backpack.add(item);
        }

        if (added) {
            shop.remove(index);
            prices.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Sells an item from the hero's backpack back to the merchant.
     * 
     * @param backpack The hero's backpack.
     * @param item     The item to sell.
     * @return The amount of gold received from the sale, or 0 if the item cannot be
     *         sold.
     */
    public int sell(BackPack backpack, Item item) {
        if (!item.isSellable())
            return 0;
        if (backpack.completelyRemoveItem(item)) {
            return calculateBasePrice(item);
        }
        return 0;
    }

    /**
     * Generates and adds a completely random item to the shop.
     * This bypasses the ItemFactory for procedural testing or variety.
     */
    public void addRandomItem() {
        Rarity[] rarities = { Rarity.COMMON, Rarity.RARE, Rarity.EPIC, Rarity.LEGENDARY };
        Rarity randomRarity = rarities[random.nextInt(rarities.length)];

        Shape randomShape = generateRandomShape();
        Item randomItem = generateRandomItem(randomRarity, randomShape);
        addItemToShop(randomItem);
    }

    /**
     * Procedurally generates a random occupancy grid (Shape) for an item.
     * 
     * @return A new randomized Shape instance.
     */
    private Shape generateRandomShape() {
        int width = 1 + random.nextInt(4);
        int height = 1 + random.nextInt(4);
        boolean[][] cells = new boolean[width][height];
        int totalCells = width * height;
        int filledCells = (int) (totalCells * (0.4 + 0.4 * random.nextDouble()));
        for (int i = 0; i < filledCells; i++) {
            int col = random.nextInt(width);
            int row = random.nextInt(height);
            cells[col][row] = true;
        }
        if (filledCells == 0) {
            cells[0][0] = true;
        }
        return new Shape(cells);
    }

    /**
     * Generates a random item instance based on specified rarity and shape.
     * 
     * @param rarity Desired rarity.
     * @param shape  Desired shape.
     * @return A newly created weapon, shield, key, or gold item.
     */
    private Item generateRandomItem(Rarity rarity, Shape shape) {
        return switch (random.nextInt(5)) {
            case 0 -> new MeleeWeapon(ItemFactory.generateId(), getRandomName(rarity, "Sword"),
                    5 + rarity.ordinal() * 10, 1,
                    true, rarity, shape, java.util.Map.of());
            case 1 -> new Shield(ItemFactory.generateId(), getRandomName(rarity, "Shield"),
                    3 + rarity.ordinal() * 5, 1,
                    true, rarity, shape, java.util.Map.of());
            case 2 -> new Key(ItemFactory.generateId(), getRandomName(rarity, "Key"), true, rarity, shape);
            case 3 -> new Bow(ItemFactory.generateId(), getRandomName(rarity, "Bow"),
                    4 + rarity.ordinal() * 8, 1,
                    true, rarity, shape, java.util.Map.of());
            default ->
                new Gold(ItemFactory.generateId(), "Gold Pouch", rarity.ordinal() * 20 + 5, false, rarity, shape);
        };
    }

    /**
     * Generates a formatted English name for an item based on its type and rarity.
     * 
     * @param rarity Item rarity.
     * @param type   The base type name (e.g., "Sword").
     * @return A string like "Mighty Sword".
     */
    private String getRandomName(Rarity rarity, String type) {
        String[] prefixes = { "Basic", "Average", "Greater", "Super" };
        return prefixes[Math.min(rarity.ordinal(), prefixes.length - 1)] + " " + type;
    }
}
