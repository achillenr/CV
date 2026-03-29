package item;

import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;
import combat.ManaShieldAction;

/**
 * Represents a magical shield that consumes mana to provide protection to the
 * hero.
 * Unlike standard shields, this requires mana points but may offer higher
 * efficiency or special scaling.
 * 
 * @param id         Unique identifier for this specimen.
 * @param name       The display name of the mana shield.
 * @param protection The amount of protection points granted when activated.
 * @param manaCost   The amount of mana consumed per activation.
 * @param isSellable Whether this shield can be traded for gold.
 * @param energyCost The energy cost associated with using the shield (usually
 *                   1).
 * @param rarity     The rarity grade affecting its value and appearance.
 * @param shape      The 2D grid shape occupying backpack slots.
 */
public record ManaShield(
        int id,
        String name,
        int protection,
        int manaCost,
        boolean isSellable,
        int energyCost,
        Rarity rarity,
        Shape shape) implements Item {

    /**
     * Compact constructor to validate mana shield attributes.
     */
    public ManaShield {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (protection < 0) {
            throw new IllegalArgumentException("protection must be >= 0");
        }
        if (manaCost < 0) {
            throw new IllegalArgumentException("manaCost must be >= 0");
        }
        if (energyCost < 0) {
            throw new IllegalArgumentException("energyCost must be >= 0");
        }
    }

    /**
     * Creates a new ManaShield instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new ManaShield instance.
     */
    @Override
    public Item withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new ManaShield(id, name, protection, manaCost, isSellable, energyCost, rarity, shape);
    }

    /**
     * Returns the combat action triggered when the hero uses this magical shield.
     * 
     * @return An Optional containing a ManaShieldAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new ManaShieldAction(protection, energyCost, manaCost, this));
    }

    /**
     * Generates a detailed tooltip description of the shield's defensive and
     * magical stats.
     * 
     * @return A formatted string for UI display.
     */
    @Override
    public String getStatDescription() {
        return "Protection: " + protection + "\nMana cost: " + manaCost;
    }

    /**
     * Returns the resale value of the mana shield based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 40;
            case RARE -> 120;
            case EPIC -> 350;
            case LEGENDARY -> 800;
            default -> (protection + manaCost) * 10;
        };
    }

    /**
     * Returns the file path to this mana shield's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "manashield.png";
    }
}
