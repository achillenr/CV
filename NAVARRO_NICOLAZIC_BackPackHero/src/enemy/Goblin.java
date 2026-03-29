package enemy;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a Goblin enemy.
 * This versatile enemy can either ATTACK or PROTECT with an equal chance each
 * turn.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its attack.
 * @param protectionGain Protection gained when using PROTECT.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record Goblin(
        String name,
        int maxHp,
        int attackDamage,
        int protectionGain,
        int expReward) implements EnemyType {

    private static final Random RNG = new Random();

    /**
     * Compact constructor to validate Goblin attributes.
     */
    public Goblin {
        Objects.requireNonNull(name, "name must not be null");

        if (maxHp <= 0)
            throw new IllegalArgumentException("maxHp must be > 0");
        if (attackDamage < 0)
            throw new IllegalArgumentException("attackDamage must be >= 0");
        if (protectionGain < 0)
            throw new IllegalArgumentException("protectionGain must be >= 0");
        if (expReward < 0)
            throw new IllegalArgumentException("expReward must be >= 0");
    }

    /**
     * Default constructor with preset stats for a standard Goblin.
     */
    public Goblin() {
        this("Goblin", 20, 4, 5, 10);
    }

    /**
     * Plans the Goblin's action for the turn.
     * Randomly chooses between a single ATTACK or double-layered PROTECT logic
     * (simplified to 1 action here).
     * 50% chance to ATTACK, 50% to PROTECT.
     *
     * @return A list containing the planned EnemyAction.
     */
    @Override
    public List<EnemyAction> planAction() {
        if (RNG.nextBoolean()) {
            return List.of(EnemyAction.ATTACK);
        } else {
            return List.of(EnemyAction.PROTECT);
        }
    }

    /**
     * Returns the texture path for the Goblin sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "gobelin.png";
    }
}
