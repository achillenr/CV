package item;

import java.util.Objects;

/**
 * Represents a pouch of gold coins collected in the dungeon.
 * Gold is the primary currency used to purchase items from merchants.
 * 
 * @param id         Unique identifier for this gold instance.
 * @param name       The display name of the gold item.
 * @param amount     The quantity of gold coins contained in this pouch.
 * @param isSellable Whether this item can be sold (usually false, as it IS
 *                   currency).
 * @param rarity     The rarity grade of the gold item.
 * @param shape      The 2D grid shape of the gold item.
 */
public record Gold(
        int id,
        String name,
        int amount,
        boolean isSellable,
        Rarity rarity,
        Shape shape) implements Item {

    /**
     * Compact constructor to validate gold attributes.
     */
    public Gold {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }

    /** Returns a new Gold object with the specified shape. */
    @Override
    public Gold withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new Gold(id, name, amount, isSellable, rarity, shape);
    }

    /**
     * Returns a simple description of the gold quantity.
     * 
     * @return A string like "Amount: 25".
     */
    @Override
    public String getStatDescription() {
        return "Amount: " + amount;
    }

    /**
     * Returns the numeric value of the gold, which is its amount.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return amount;
    }

    /**
     * Returns the file path to the gold's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "gold.png";
    }

    /**
     * Gold pouches do not consume mana.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }

    /**
     * Carrying or selecting gold pouches does not consume energy.
     * 
     * @return 0.
     */
    @Override
    public int energyCost() {
        return 0;
    }
}
