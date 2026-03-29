package item;

import java.util.Objects;

/**
 * Represents a key item used to unlock doors and chests within the dungeon.
 * Keys are typically single-use utility items that occupy space in the
 * backpack.
 * 
 * @param id         Unique identifier for the key instance.
 * @param name       The name of the key (e.g., "Rusty Key").
 * @param isSellable Whether the key can be traded for gold.
 * @param rarity     The rarity grade of the key.
 * @param shape      The 2D grid shape of the key.
 */
public record Key(int id,
        String name,
        boolean isSellable,
        Rarity rarity,
        Shape shape) implements Item {

    /**
     * Compact constructor to validate key attributes.
     */
    public Key {
        if (id < 0)
            throw new IllegalArgumentException("id cannot be negative");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");
    }

    /** Returns a new Key with the specified shape. */
    @Override
    public Key withShape(Shape shape) {
        return new Key(id, name, isSellable, rarity, shape);
    }

    /**
     * Provides a flavor description for the key.
     * 
     * @return An English description string.
     */
    @Override
    public String getStatDescription() {
        return "An ancient key capable of opening complex locks.\nSingle use.";
    }

    /**
     * Returns the fixed resale value of a key.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return 50;
    }

    /**
     * Returns the file path to the key's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "key.png";
    }

    /**
     * Keys do not consume energy to carry or select.
     * 
     * @return 0.
     */
    @Override
    public int energyCost() {
        return 0;
    }

    /**
     * Keys do not consume mana.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }
}
