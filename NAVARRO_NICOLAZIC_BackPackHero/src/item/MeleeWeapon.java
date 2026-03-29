package item;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;
import combat.Status;

/**
 * Represents a standard melee weapon that deals direct damage to enemies.
 * Weapons can optionally apply status effects (like poison or burn) upon
 * hitting.
 * 
 * @param id         Unique identifier for this specimen.
 * @param name       The display name of the weapon.
 * @param damage     Amount of damage dealt per attack.
 * @param energyCost Energy consumed when performing an attack.
 * @param isSellable Whether this weapon can be sold to a merchant.
 * @param rarity     The rarity grade affecting its appearance and value.
 * @param shape      The 2D grid shape of the item in the backpack.
 * @param effects    A map of status effects applied to the target on hit.
 */
public record MeleeWeapon(
        int id,
        String name,
        int damage,
        int energyCost,
        boolean isSellable,
        Rarity rarity,
        Shape shape,
        Map<Status, Integer> effects) implements Item {

    /**
     * Compact constructor to validate melee weapon attributes.
     */
    public MeleeWeapon {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (damage < 0) {
            throw new IllegalArgumentException("damage must be >= 0");
        }
        if (energyCost < 0) {
            throw new IllegalArgumentException("energyCost must be >= 0");
        }

        effects = Objects.requireNonNullElse(effects, Map.of());
    }

    /**
     * Creates a new MeleeWeapon instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new MeleeWeapon instance.
     */
    @Override
    public MeleeWeapon withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new MeleeWeapon(id, name, damage, energyCost, isSellable, rarity, shape, effects);
    }

    /**
     * Generates a detailed tooltip description of the weapon's stats and special
     * effects.
     * 
     * @return A formatted string for UI display.
     */
    @Override
    public String getStatDescription() {
        StringBuilder sb = new StringBuilder("Damage: ").append(damage);
        if (!effects.isEmpty()) {
            sb.append("\nEffects:");
            effects.forEach((s, v) -> sb.append("\n - ")
                    .append(s.getDisplayName())
                    .append(" ")
                    .append(v)
                    .append(": ")
                    .append(s.getTooltip(v)));
        }
        return sb.toString();
    }

    /**
     * Returns the resale value of the weapon based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 20;
            case RARE -> 75;
            case EPIC -> 200;
            case LEGENDARY -> 500;
            default -> damage * 5;
        };
    }

    /**
     * Returns the combat action triggered when this weapon is used in battle.
     * 
     * @return An Optional containing an AttackAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new combat.AttackAction(damage, energyCost, effects));
    }

    /**
     * Returns the file path to this weapon's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "meleeweapon.png";
    }

    /**
     * Melee weapons do not consume mana by default.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }
}
