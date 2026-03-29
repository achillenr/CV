package item;

import java.util.Random;

/**
 * Factory class responsible for creating all items in the game.
 * This includes random treasures, enemy loot, and specialized equipment of
 * varying rarities.
 */
public class ItemFactory {

    private static final Random RANDOM = new Random();

    /**
     * Generates a unique identifier for a new item.
     * 
     * @return A random integer.
     */
    public static int generateId() {
        return RANDOM.nextInt(Integer.MAX_VALUE);
    }

    // Probabilities
    private static final int GOLD_PROBABILITY = 30; // 30% chance for gold
    private static final int MIN_GOLD = 5;
    private static final int MAX_GOLD = 25;

    // --- MAIN GENERATION ---

    /**
     * Creates a random item suitable for a treasure chest or shop.
     * 
     * @return A random Item (Gold or Equipment).
     */
    public static Item createRandomTreasure() {
        int roll = RANDOM.nextInt(100);

        // 1. Gold Chance (30%)
        if (roll < GOLD_PROBABILITY) {
            int amount = MIN_GOLD + RANDOM.nextInt(MAX_GOLD - MIN_GOLD + 1);
            return createGold(amount);
        }

        // 2. Equipment Chance (70%)
        return createRandomEquipment();
    }

    /**
     * Creates a loot item dropped by an enemy upon defeat.
     * 
     * @param enemyExpReward The experience value of the defeated enemy, used to
     *                       scale gold loot.
     * @return A random Item (weighted towards gold for enemies).
     */
    public static Item createEnemyLoot(int enemyExpReward) {
        int roll = RANDOM.nextInt(100);

        // 70% Gold for enemies
        if (roll < 70) {
            int baseGold = Math.max(5, enemyExpReward / 2);
            int goldAmount = baseGold + RANDOM.nextInt(Math.max(1, baseGold / 2));
            return createGold(goldAmount);
        }

        // 30% Equipment
        return createRandomEquipment();
    }

    /**
     * Creates a random piece of equipment based on calculated rarity rolls.
     * 
     * @return A random equipment Item.
     */
    private static Item createRandomEquipment() {
        int rarityRoll = RANDOM.nextInt(100);

        if (rarityRoll < 50)
            return createCommonItem();
        if (rarityRoll < 75)
            return createRareItem();
        if (rarityRoll < 90)
            return createEpicItem();
        return createLegendaryItem();
    }

    // --- LOOT TABLES BY RARITY ---

    /**
     * Creates an item of Rare rarity or higher.
     * 
     * @return A Rare, Epic, or Legendary Item.
     */
    public static Item createUncommonItem() {
        int roll = RANDOM.nextInt(100);
        // 50% Rare, 30% Epic, 20% Legendary relative to this pool
        if (roll < 50)
            return createRareItem();
        if (roll < 80)
            return createEpicItem();
        return createLegendaryItem();
    }

    /**
     * Creates a common rarity item from a pool of swords, shields, mana stones,
     * etc.
     * 
     * @return A COMMON rarity Item.
     */
    private static Item createCommonItem() {
        int type = RANDOM.nextInt(6);
        return switch (type) {
            case 0 -> createStandardSword();
            case 1 -> createStandardShield();
            case 2 -> createSmallManaStone();
            case 3 -> createSimpleKey();
            case 4 -> createStandardBow();
            default -> createHealthPotion();
        };
    }

    /**
     * Creates a rare rarity item like wands, reinforced shields, or mana staves.
     * 
     * @return A RARE rarity Item.
     */
    private static Item createRareItem() {
        int type = RANDOM.nextInt(7);
        return switch (type) {
            case 0 -> createMagicWand();
            case 1 -> createReinforcedShield();
            case 2 -> createManaStaff();
            case 3 -> createRunicShield();
            case 4 -> createBronzeKey();
            case 5 -> createMagicBow();
            default -> createEnergyPotion();
        };
    }

    /**
     * Creates an epic rarity item such as dragon lances or arcane scepters.
     * 
     * @return An EPIC rarity Item.
     */
    private static Item createEpicItem() {
        int type = RANDOM.nextInt(4);
        return switch (type) {
            case 0 -> createDragonLance();
            case 1 -> createGrandManaStone();
            case 2 -> createArcaneScepter();
            default -> createProtectionScroll();
        };
    }

    /**
     * Creates a legendary rarity item (e.g., hero relics).
     * 
     * @return A LEGENDARY rarity Item.
     */
    private static Item createLegendaryItem() {
        if (RANDOM.nextBoolean()) {
            return createHeroRelic();
        } else {
            return createInfinityManaGem();
        }
    }

    // --- SPECIFIC ITEM CREATORS ---

    /**
     * Generates a random set of status effects based on the item's rarity.
     * 
     * @param rarity The rarity of the item being generated.
     * @return A map of status effects and their intensities.
     */
    private static java.util.Map<combat.Status, Integer> generateRandomEffects(Rarity rarity) {
        java.util.Map<combat.Status, Integer> effects = new java.util.HashMap<>();
        if (RANDOM.nextInt(100) < getEffectChance(rarity)) {
            effects.put(getRandomStatus(), getEffectAmount(rarity));
        }
        return effects;
    }

    /**
     * Returns the percentage chance of an item having a special status effect based
     * on rarity.
     * 
     * @param rarity Item rarity.
     * @return Chance (0-100).
     */
    private static int getEffectChance(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 10;
            case RARE -> 30;
            case EPIC -> 60;
            case LEGENDARY -> 90;
            default -> 0;
        };
    }

    /**
     * Selects a random negative status effect for weapon application.
     * 
     * @return A random Status.
     */
    private static combat.Status getRandomStatus() {
        combat.Status[] p = { combat.Status.POISON, combat.Status.BURN, combat.Status.WEAK, combat.Status.SLOW };
        return p[RANDOM.nextInt(p.length)];
    }

    /**
     * Determines the potency of status effects based on rarity.
     * 
     * @param rarity Item rarity.
     * @return Number of stacks/intensity.
     */
    private static int getEffectAmount(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case RARE -> 2;
            case EPIC -> 3;
            case LEGENDARY -> 5;
            default -> 1;
        };
    }

    // COMMON ITEMS
    /** Creates a standard iron sword. */
    public static Item createStandardSword() {
        boolean[][] s = { { true }, { true }, { true } }; // 1x3
        return new MeleeWeapon(generateId(), "Iron Sword", 10, 1, true, Rarity.COMMON, new Shape(s),
                generateRandomEffects(Rarity.COMMON));
    }

    /** Creates a standard short bow. */
    public static Item createStandardBow() {
        boolean[][] s = { { true, true }, { true, false }, { true, true } }; // C-shape
        return new Bow(generateId(), "Short Bow", 8, 1, true, Rarity.COMMON, new Shape(s),
                generateRandomEffects(Rarity.COMMON));
    }

    /** Creates a standard wooden shield. */
    public static Item createStandardShield() {
        boolean[][] s = { { true, true }, { true, true } }; // 2x2
        return new Shield(generateId(), "Wooden Shield", 15, 1, true, Rarity.COMMON, new Shape(s),
                generateRandomEffects(Rarity.COMMON));
    }

    /** Creates a basic mana stone. */
    public static Item createSmallManaStone() {
        boolean[][] s = { { true } }; // 1x1
        return new ManaStone(generateId(), "Weak Mana Stone", 10, true, 0, Rarity.COMMON, new Shape(s), 0);
    }

    /** Creates a simple rusty key. */
    public static Item createSimpleKey() {
        boolean[][] s = { { true } }; // 1x1
        return new Key(generateId(), "Rusty Key", true, Rarity.COMMON, new Shape(s));
    }

    // RARE ITEMS
    /** Creates a basic magic wand. */
    public static Item createMagicWand() {
        boolean[][] s = { { true }, { true } }; // 1x2
        return new MeleeWeapon(generateId(), "Apprentice Wand", 15, 1, true, Rarity.RARE, new Shape(s),
                generateRandomEffects(Rarity.RARE));
    }

    /** Creates a composite magic bow. */
    public static Item createMagicBow() {
        boolean[][] s = { { false, true, true }, { true, false, false }, { false, true, true } };
        return new Bow(generateId(), "Composite Bow", 18, 1, true, Rarity.RARE, new Shape(s),
                generateRandomEffects(Rarity.RARE));
    }

    /** Creates an iron reinforced shield. */
    public static Item createReinforcedShield() {
        boolean[][] s = { { true, true }, { true, true } }; // 2x2
        return new Shield(generateId(), "Iron Shield", 25, 1, true, Rarity.RARE, new Shape(s),
                generateRandomEffects(Rarity.RARE));
    }

    /** Creates a basic mage staff. */
    public static Item createManaStaff() {
        boolean[][] s = { { true }, { true }, { true } }; // 1x3
        // 2 mana cost, 12 damage, 0 energy cost
        return new ManaWeapon(generateId(), "Mage Staff", 2, 12, true, 0, Rarity.RARE, new Shape(s));
    }

    /** Creates a rundo-enhanced shield. */
    public static Item createRunicShield() {
        boolean[][] s = { { true, true }, { true, true } }; // 2x2
        // 20 protection, 3 mana cost, 0 energy cost
        return new ManaShield(generateId(), "Runic Shield", 20, 3, true, 0, Rarity.RARE, new Shape(s));
    }

    /** Creates a bronze dungeon key. */
    public static Item createBronzeKey() {
        boolean[][] s = { { true }, { true } }; // 1x2
        return new Key(generateId(), "Bronze Key", true, Rarity.RARE, new Shape(s));
    }

    // EPIC ITEMS
    /** Creates a powerful dragon lance. */
    public static Item createDragonLance() {
        boolean[][] s = { { true }, { true }, { true }, { true } }; // 1x4
        return new MeleeWeapon(generateId(), "Dragon Lance", 30, 2, true, Rarity.EPIC, new Shape(s),
                generateRandomEffects(Rarity.EPIC));
    }

    /** Creates a high-capacity mana stone. */
    public static Item createGrandManaStone() {
        boolean[][] s = { { true, true } }; // 2x1
        return new ManaStone(generateId(), "Grand Mana Stone", 30, true, 0, Rarity.EPIC, new Shape(s), 0);
    }

    /** Creates an arcane magic scepter. */
    public static Item createArcaneScepter() {
        boolean[][] s = { { true, true }, { false, true }, { false, true } }; // Scepter shape
        // 5 mana cost, 25 damage, 0 energy cost
        return new ManaWeapon(generateId(), "Arcane Scepter", 5, 25, true, 0, Rarity.EPIC, new Shape(s));
    }

    // LEGENDARY ITEMS
    /** Creates a mythical hero relic. */
    public static Item createHeroRelic() {
        boolean[][] s = {
                { false, true, false },
                { true, true, true },
                { false, true, false }
        }; // Cross shape
        return new MeleeWeapon(generateId(), "Hero's Relic", 50, 3, true, Rarity.LEGENDARY, new Shape(s),
                generateRandomEffects(Rarity.LEGENDARY));
    }

    /** Creates a limitless mana gem. */
    public static Item createInfinityManaGem() {
        boolean[][] s = { { true, true }, { true, true } }; // 2x2
        return new ManaStone(generateId(), "Infinity Gem", 100, true, 0, Rarity.LEGENDARY, new Shape(s), 0);
    }

    /**
     * Creates a gold pouch item.
     * 
     * @param amount The quantity of gold.
     * @return A Gold item instance.
     */
    public static Item createGold(int amount) {
        boolean[][] s = { { true } };
        return new Gold(generateId(), "Gold Pouch", amount, false, Rarity.COMMON, new Shape(s));
    }

    // CONSUMABLES
    /** Creates a basic health potion. */
    public static Item createHealthPotion() {
        boolean[][] s = { { true } };
        return new Consumable(generateId(), "Health Potion", Rarity.COMMON, new Shape(s), ConsumableType.HEALTH, 10);
    }

    /** Creates an energy restoring potion. */
    public static Item createEnergyPotion() {
        boolean[][] s = { { true } };
        return new Consumable(generateId(), "Energy Potion", Rarity.RARE, new Shape(s), ConsumableType.ENERGY, 2);
    }

    /** Creates a magical protection scroll. */
    public static Item createProtectionScroll() {
        boolean[][] s = { { true }, { true } };
        return new Consumable(generateId(), "Protection Scroll", Rarity.EPIC, new Shape(s), ConsumableType.PROTECTION,
                15);
    }

    /** Creates a mana restoring potion. */
    public static Item createManaPotion() {
        boolean[][] s = { { true } };
        return new Consumable(generateId(), "Mana Potion", Rarity.RARE, new Shape(s), ConsumableType.MANA, 10);
    }

}
