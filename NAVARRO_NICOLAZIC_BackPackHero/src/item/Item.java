package item;

import java.util.Optional;
import combat.CombatAction;

/**
 * Core interface representing any object that can occupy space in the hero's
 * backpack.
 * Items can be equipment (weapons, shields), resources (mana stones, gold),
 * consumables, or utility objects (keys, curses).
 */
public sealed interface Item
        permits Gold, MeleeWeapon, Shield, Key, Curse, ManaStone, ManaWeapon, ManaShield, Consumable, Bow {

    /** @return Unique instance identifier assigned at creation. */
    int id();

    /** @return Player-facing name of the item. */
    String name();

    /** @return true if the item can be sold to a merchant. */
    boolean isSellable();

    /** @return Cost in energy points to activate/use the item in combat. */
    int energyCost();

    /** @return Rarity grade affecting value and UI appearance. */
    Rarity rarity();

    /** @return Current 2D grid orientation and occupancy. */
    Shape shape();

    /**
     * Returns a new instance with the specified shape/orientation.
     * 
     * @param shape The new orientation.
     * @return New Item instance.
     */
    Item withShape(Shape shape);

    /**
     * Returns the resale value of the item in gold.
     * 
     * @return Value amount.
     */
    default int getValue() {
        return 10;
    }

    /**
     * @return Formatted string describing the item's primary stats (e.g., "Damage:
     *         10").
     */
    String getStatDescription();

    /**
     * Returns the combat behavior logic associated with this item.
     * 
     * @return Optional containing the action, or empty if unusable in combat.
     */
    default Optional<CombatAction> combatAction() {
        return Optional.empty();
    }

    /** @return File path to the item's texture image. */
    String getTexturePath();

    /** @return Cost in mana points to activate/use the item. */
    int manaCost();

    /**
     * Returns whether the item can be manually moved by the player in the backpack
     * UI.
     * 
     * @return true if draggable.
     */
    default boolean isDraggable() {
        return true;
    }

    /**
     * Returns whether the item can be manually rotated by the player.
     * 
     * @return true if rotatable.
     */
    default boolean isRotatable() {
        return true;
    }

    /**
     * Special check for ManaStone capability without using instanceof.
     * 
     * @return true if the item is a mana source.
     */
    default boolean isManaStone() {
        return false;
    }

    /**
     * Safely casts this item to a ManaStone.
     * 
     * @return This instance as a ManaStone.
     * @throws UnsupportedOperationException if this is not a ManaStone.
     */
    default ManaStone asManaStone() {
        throw new UnsupportedOperationException("Item is not a ManaStone: " + getClass().getName());
    }

    /**
     * Renders an item-specific visual overlay (e.g., mana counts, status icons).
     * 
     * @param g        Background graphics context.
     * @param x        Grid slot X screen coordinate.
     * @param y        Grid slot Y screen coordinate.
     * @param cellSize size of a single grid cell in pixels.
     */
    default void renderOverlay(java.awt.Graphics2D g, int x, int y, int cellSize) {
        // Default: no overlay
    }
}
