package item;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;
import combat.Status;

/**
 * Represents a ranged Bow weapon.
 * Bows deal physical damage and can apply status effects, similar to melee
 * weapons but with different scaling.
 * 
 * @param id         Unique identifier for this specific bow instance.
 * @param name       The display name of the bow.
 * @param damage     Direct damage dealt to the target.
 * @param energyCost Energy consumed per shot.
 * @param isSellable Whether this bow can be sold to the merchant.
 * @param rarity     The rarity grade affecting its value and power.
 * @param shape      The 2D occupancy grid in the backpack.
 * @param effects    A map of status effects applied on a successful hit.
 */
public record Bow(
        int id,
        String name,
        int damage,
        int energyCost,
        boolean isSellable,
        Rarity rarity,
        Shape shape,
        Map<Status, Integer> effects) implements Item {

    /**
     * Compact constructor to validate bow attributes and effects.
     */
    public Bow {
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
     * Creates a new Bow instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new Bow instance.
     */
    @Override
    public Bow withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new Bow(id, name, damage, energyCost, isSellable, rarity, shape, effects);
    }

    /**
     * Generates a detailed tooltip description of the bow's stats and effects.
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
     * Returns the resale value of the bow based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 25;
            case RARE -> 80;
            case EPIC -> 220;
            case LEGENDARY -> 550;
            default -> damage * 6;
        };
    }

    /**
     * Returns the combat action triggered when the hero uses this bow.
     * 
     * @return An Optional containing an AttackAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new combat.AttackAction(damage, energyCost, effects));
    }

    /**
     * Returns the file path to this bow's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "bow.png";
    }

    /**
     * Bows do not consume mana by default.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }
}
