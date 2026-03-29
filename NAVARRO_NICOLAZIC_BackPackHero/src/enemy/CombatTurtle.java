package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Combat Turtle enemy.
 * This defensive enemy always focuses on gaining protection.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its attack (minimal).
 * @param protectionGain Protection gained per turn.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record CombatTurtle(String name, int maxHp, int attackDamage, int protectionGain, int expReward)
        implements EnemyType {

    /**
     * Compact constructor to validate Combat Turtle attributes.
     */
    public CombatTurtle {
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
     * Default constructor with preset stats for a standard Combat Turtle.
     */
    public CombatTurtle() {
        this("Combat Turtle", 15, 1, 3, 15);
    }

    /**
     * Plans the Combat Turtle's action for the turn.
     * Always returns a PROTECT action.
     * 
     * @return A list containing the PROTECT action.
     */
    @Override
    public List<EnemyAction> planAction() {
        return List.of(EnemyAction.PROTECT);
    }

    /**
     * Returns the texture path for the Combat Turtle sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "battletortoise.png";
    }
}
