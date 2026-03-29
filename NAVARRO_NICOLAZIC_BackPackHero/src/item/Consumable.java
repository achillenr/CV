package item;

import java.util.Objects;
import java.util.Optional;
import combat.CombatAction;
import main.Combat;
import hero.Hero;

/**
 * Represents a single-use consumable item like a potion or scroll.
 * Using a consumable often triggers an immediate effect (healing, mana gain,
 * etc.)
 * and consumes the item.
 * 
 * @param id     Unique identifier for the item.
 * @param name   Display name of the consumable.
 * @param rarity Rarity affecting its appearance and value.
 * @param shape  2D grid shape in the backpack.
 * @param type   The category of effect (HEALTH, ENERGY, etc.).
 * @param power  The numerical potency of the effect.
 */
public record Consumable(
        int id,
        String name,
        Rarity rarity,
        Shape shape,
        ConsumableType type,
        int power) implements Item {

    /**
     * Compact constructor to validate consumable attributes.
     */
    public Consumable {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(shape, "shape cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
    }

    /**
     * Consumables are generally sellable.
     * 
     * @return true.
     */
    @Override
    public boolean isSellable() {
        return true;
    }

    /**
     * Energy cost to use this item during a combat turn.
     * 
     * @return 1 energy point.
     */
    @Override
    public int energyCost() {
        return 1;
    }

    /**
     * Creates a new Consumable instance with a different orientation/shape.
     * 
     * @param newShape The new Shape to apply.
     * @return A new Consumable instance.
     */
    @Override
    public Item withShape(Shape newShape) {
        return new Consumable(id, name, rarity, newShape, type, power);
    }

    /**
     * Returns a description of the consumable's effect and potency.
     * 
     * @return Formatted string for UI tooltips.
     */
    @Override
    public String getStatDescription() {
        return type.getDescription() + ": " + power;
    }

    /**
     * Returns the file path to this consumable's unique texture set.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return switch (type) {
            case HEALTH -> "potion_health.png";
            case ENERGY -> "potion_energy.png";
            case PROTECTION -> "scroll_protection.png";
            case MANA -> "potion_mana.png";
        };
    }

    /**
     * Returns the resale value of the consumable based on its rarity grade.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return switch (rarity) {
            case COMMON -> 10;
            case RARE -> 30;
            case EPIC -> 100;
            case LEGENDARY -> 250;
            default -> 10;
        };
    }

    /**
     * Consumables do not consume mana.
     * 
     * @return 0.
     */
    @Override
    public int manaCost() {
        return 0;
    }

    /**
     * Returns the combat action that triggers the consumable's effect and removes
     * it after use.
     * 
     * @return An Optional containing the discrete CombatAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new CombatAction() {
            @Override
            public boolean needsTarget() {
                return false;
            }

            @Override
            public int energyCost() {
                return 1;
            }

            @Override
            public boolean execute(Combat combat, int targetIndex) {
                applyEffect(combat.getHero());
                combat.getHero().getBackPack().completelyRemoveItem(Consumable.this);
                return true;
            }
        });
    }

    /**
     * Core logic for applying the item's benefit to the hero.
     * 
     * @param hero The hero receiving the effect.
     */
    public void applyEffect(Hero hero) {
        switch (type) {
            case HEALTH -> hero.heal(power);
            case ENERGY -> hero.setEnergy(hero.getEnergy() + power); // Clamped in setter
            case PROTECTION -> hero.increaseProtection(power);
            case MANA -> hero.setMana(hero.getMana() + power);
        }
    }
}
