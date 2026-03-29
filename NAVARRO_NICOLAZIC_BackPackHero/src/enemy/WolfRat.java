package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Wolf-Rat enemy.
 * This more dangerous variant attacks twice every turn.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt per attack.
 * @param protectionGain Protection gained when using PROTECT (unused here).
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record WolfRat(String name, int maxHp, int attackDamage, int protectionGain, int expReward)
        implements EnemyType {

    /**
     * Compact constructor to validate Wolf-Rat attributes.
     */
    public WolfRat {
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
     * Default constructor with preset stats for a standard Wolf-Rat.
     */
    public WolfRat() {
        this("Wolf-Rat", 20, 4, 5, 10);
    }

    /**
     * Plans the Wolf-Rat's actions for the turn.
     * This enemy always executes two ATTACK actions.
     *
     * @return A list containing two ATTACK actions.
     */
    @Override
    public List<EnemyAction> planAction() {
        return List.of(EnemyAction.ATTACK, EnemyAction.ATTACK);
    }

    /**
     * Returns the texture path for the Wolf-Rat sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "bigrat.png";
    }
}
