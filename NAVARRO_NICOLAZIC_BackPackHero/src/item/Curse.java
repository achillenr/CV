package item;

import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;

/**
 * Represents a Cursed item that clutters the hero's backpack.
 * Curses cannot be moved, rotated, or sold by the player once placed.
 * They have a negative value and serve as obstacles in inventory management.
 * 
 * @param id         Unique identifier for this specific curse instance.
 * @param name       The name of the curse (e.g., "Dark Chain").
 * @param curseLevel The severity of the curse, affecting its negative value.
 * @param rarity     The rarity grade of the curse.
 * @param shape      The fixed 2D grid shape occupying backpack slots.
 */
public record Curse(
        int id,
        String name,
        int curseLevel,
        Rarity rarity,
        Shape shape) implements Item {

    /**
     * Compact constructor to validate curse attributes.
     */
    public Curse {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (curseLevel <= 0) {
            throw new IllegalArgumentException("curseLevel must be > 0");
        }

        if (id < 0) {
            throw new IllegalArgumentException("id must be positive");
        }
    }

    /**
     * Creates a new Curse instance with a different orientation/shape.
     * Note: While curses are non-rotatable by the player, the system may use this
     * for initial placement.
     * 
     * @param newShape The new Shape to apply.
     * @return A new Curse instance.
     */
    @Override
    public Curse withShape(Shape newShape) {
        Objects.requireNonNull(newShape, "shape cannot be null");
        return new Curse(id, name, curseLevel, rarity, newShape);
    }

    /**
     * Provides a description of the curse's intensity.
     * 
     * @return Formatted string like "Curse level: 2".
     */
    @Override
    public String getStatDescription() {
        return "Curse level: " + curseLevel;
    }

    /**
     * Curses are detrimental and cannot be sold to merchants.
     * 
     * @return false.
     */
    @Override
    public boolean isSellable() {
        return false;
    }

    /**
     * Curses do not consume energy as they cannot be used actively.
     * 
     * @return 0.
     */
    @Override
    public int energyCost() {
        return 0;
    }

    /**
     * Returns a negative value proportional to the curse level.
     * This represents the cost or penalty associated with carrying the curse.
     * 
     * @return Negative integer value.
     */
    @Override
    public int getValue() {
        return -curseLevel * 50;
    }

    /**
     * Curses provide no combat utility.
     * 
     * @return An empty Optional.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.empty();
    }

    /**
     * Returns the file path to this curse's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "curse.png";
    }

    /**
     * Curses do not consume mana.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }

    /**
     * Curses are fixed in the backpack and cannot be dragged by the player.
     * 
     * @return false.
     */
    @Override
    public boolean isDraggable() {
        return false;
    }

    /**
     * Curses are fixed in the backpack and cannot be rotated by the player.
     * 
     * @return false.
     */
    @Override
    public boolean isRotatable() {
        return false;
    }
}
