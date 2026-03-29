package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Interface representing a template for an enemy type.
 * Defines base stats, action planning logic, and assets.
 */
public sealed interface EnemyType permits WolfRat, SmallWolfRat, CursedSpecter, CombatTurtle, Goblin, LivingShadow,
        BeeQueen, FrogWizard {
    /** Returns the name of the enemy. */
    String name();

    /** Returns the maximum HP for this enemy type. */
    int maxHp();

    /** Returns the base attack damage value. */
    int attackDamage();

    /** Returns the base protection gained from a PROTECT action. */
    int protectionGain();

    /**
     * Returns the experience points granted when an enemy of this type is defeated.
     */
    int expReward();

    /** Returns a list of actions this enemy type intends to take. */
    List<EnemyAction> planAction();

    /**
     * Validates that an EnemyType's attributes are correctly initialized.
     *
     * @param type the EnemyType instance to validate.
     * @throws NullPointerException  if type or any required field is null.
     * @throws IllegalStateException if numeric values are out of valid bounds.
     */
    static void validate(EnemyType type) {
        Objects.requireNonNull(type, "EnemyType cannot be null");

        Objects.requireNonNull(type.name(), "Enemy name is null");
        Objects.requireNonNull(type.planAction(), "Action list is null");

        if (type.maxHp() <= 0) {
            throw new IllegalStateException("maxHp must be > 0");
        }
        if (type.attackDamage() < 0) {
            throw new IllegalStateException("attackDamage cannot be negative");
        }
        if (type.protectionGain() < 0) {
            throw new IllegalStateException("protectionGain cannot be negative");
        }
        if (type.expReward() <= 0) {
            throw new IllegalStateException("expReward must be > 0");
        }
    }

    /** Returns the path to the enemy's texture file. */
    String getTexturePath();
}