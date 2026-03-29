package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Small Wolf-Rat enemy.
 * This basic enemy always ATTACKS each turn.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its attack.
 * @param protectionGain Protection gained when using PROTECT (unused here).
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record SmallWolfRat(String name, int maxHp, int attackDamage, int protectionGain, int expReward)
        implements EnemyType {

    /**
     * Compact constructor to validate Small Wolf-Rat attributes.
     */
    public SmallWolfRat {
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
     * Default constructor with preset stats for a standard Small Wolf-Rat.
     */
    public SmallWolfRat() {
        this("Small Wolf-Rat", 10, 2, 3, 5);
    }

    /**
     * Plans the Small Wolf-Rat's action for the turn.
     * Always returns a single ATTACK action.
     *
     * @return A list containing the planned EnemyAction.
     */
    @Override
    public List<EnemyAction> planAction() {
        return List.of(EnemyAction.ATTACK);
    }

    /**
     * Returns the texture path for the Small Wolf-Rat sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "littlerat.png";
    }
}
