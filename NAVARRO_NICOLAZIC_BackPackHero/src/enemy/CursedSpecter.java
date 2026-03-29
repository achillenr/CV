package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Cursed Specter enemy.
 * This ethereal enemy primarily uses CURSE actions to hinder the hero.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Direct damage dealt (minimal or zero).
 * @param protectionGain Protection gained per turn.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record CursedSpecter(String name, int maxHp, int attackDamage, int protectionGain, int expReward)
        implements EnemyType {

    /**
     * Compact constructor to validate Cursed Specter attributes.
     */
    public CursedSpecter {
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
     * Default constructor with preset stats for a standard Cursed Specter.
     */
    public CursedSpecter() {
        this("Cursed Specter", 18, 0, 0, 20);
    }

    /**
     * Plans the Cursed Specter's action for the turn.
     * Always returns a CURSE action.
     * 
     * @return A list containing the CURSE action.
     */
    @Override
    public List<EnemyAction> planAction() {
        return List.of(EnemyAction.CURSE);
    }

    /**
     * Returns the texture path for the Cursed Specter sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "spectremaudit.png";
    }
}
