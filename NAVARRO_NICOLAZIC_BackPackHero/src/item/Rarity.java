package item;

/**
 * Represents the rarity of an item, determining its value and spawn rate.
 */
public enum Rarity {
    /** Common items, easy to find. */
    COMMON,
    /** Rare items, offering better stats. */
    RARE,
    /** Epic items, often with unique effects. */
    EPIC,
    /** Legendary items, the most powerful. */
    LEGENDARY,

    /** Special rarity for dungeon keys. */
    KEY,
    /** Special rarity for gold piles. */
    GOLD,
    /** Special rarity for cursed items. */
    CURSE
}
