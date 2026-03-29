package item;

import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;
import combat.ManaAttackAction;

/**
 * Represents a magical weapon that consumes mana to deal damage.
 * These weapons typically require a connection to a ManaStone in the backpack.
 * 
 * @param id         Unique identifier for this weapon instance.
 * @param name       The display name of the mana weapon.
 * @param manaCost   The amount of mana consumed per strike.
 * @param damage     The raw damage output of the weapon.
 * @param isSellable Whether the weapon can be sold to a merchant.
 * @param energyCost The energy cost to swing the weapon (usually 1).
 * @param rarity     The rarity grade affecting value and stats.
 * @param shape      The 2D grid shape occupying backpack slots.
 */
public record ManaWeapon(
        int id,
        String name,
        int manaCost,
        int damage,
        boolean isSellable,
        int energyCost,
        Rarity rarity,
        Shape shape) implements Item {

    /**
     * Compact constructor to validate mana weapon attributes.
     */
    public ManaWeapon {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (manaCost < 0) {
            throw new IllegalArgumentException("manaCost must be >= 0");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("damage must be >= 0");
        }
        if (energyCost < 0) {
            throw new IllegalArgumentException("energyCost must be >= 0");
        }
    }

    /**
     * Creates a new ManaWeapon instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new ManaWeapon instance.
     */
    @Override
    public Item withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new ManaWeapon(id, name, manaCost, damage, isSellable, energyCost, rarity, shape);
    }

    /**
     * Returns the combat action triggered when the hero attacks with this magical
     * weapon.
     * 
     * @return An Optional containing a ManaAttackAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new ManaAttackAction(damage, energyCost, manaCost, this));
    }

    /**
     * Generates a detailed tooltip description of the weapon's offensive and
     * magical stats.
     * 
     * @return A formatted string for UI display.
     */
    @Override
    public String getStatDescription() {
        return "Mana cost: " + manaCost + "\nDamage: " + damage;
    }

    /**
     * Returns the resale value of the mana weapon based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 50;
            case RARE -> 150;
            case EPIC -> 400;
            case LEGENDARY -> 1000;
            default -> (damage * 5) + (manaCost * 2);
        };
    }

    /**
     * Returns the file path to this mana weapon's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "manaweapon.png";
    }
}
