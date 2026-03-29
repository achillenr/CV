package item;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import combat.CombatAction;
import combat.ShieldAction;
import combat.Status;

/**
 * Represents a Shield item used to gain protection in combat.
 * Blocking consume energy and grants temporary protection points to the hero.
 * 
 * @param id         Unique identifier for this specimen.
 * @param name       The display name of the shield.
 * @param protection The amount of protection points granted when used.
 * @param energyCost Energy consumed when performing a block.
 * @param isSellable Whether this shield can be sold to a merchant.
 * @param rarity     The rarity grade affecting its appearance and value.
 * @param shape      The 2D grid shape of the item in the backpack.
 * @param effects    A map of special status effects (if any) associated with
 *                   the shield.
 */
public record Shield(
        int id,
        String name,
        int protection,
        int energyCost,
        boolean isSellable,
        Rarity rarity,
        Shape shape,
        Map<Status, Integer> effects) implements Item {

    /**
     * Compact constructor to validate shield attributes.
     */
    public Shield {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");

        if (protection < 0) {
            throw new IllegalArgumentException("protection must be >= 0");
        }
        if (energyCost < 0) {
            throw new IllegalArgumentException("energyCost must be >= 0");
        }

        effects = Objects.requireNonNullElse(effects, Map.of());
    }

    /**
     * Creates a new Shield instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new Shield instance.
     */
    @Override
    public Shield withShape(Shape shape) {
        Objects.requireNonNull(shape, "shape cannot be null");
        return new Shield(id, name, protection, energyCost, isSellable, rarity, shape, effects);
    }

    /**
     * Generates a detailed tooltip description of the shield's defensive stats.
     * 
     * @return A formatted string for UI display.
     */
    @Override
    public String getStatDescription() {
        StringBuilder sb = new StringBuilder("Protection: ").append(protection);
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
     * Returns the resale value of the shield based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 15;
            case RARE -> 50;
            case EPIC -> 150;
            case LEGENDARY -> 400;
            default -> protection * 4;
        };
    }

    /**
     * Returns the combat action triggered when the hero uses this shield to block.
     * 
     * @return An Optional containing a ShieldAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new ShieldAction(protection, energyCost));
    }

    /**
     * Returns the file path to this shield's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "shield.png";
    }

    /**
     * Shields do not consume mana by default.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }
}
