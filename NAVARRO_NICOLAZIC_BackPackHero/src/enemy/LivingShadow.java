package enemy;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a Living Shadow enemy.
 * An elusive foe that can both ATTACK and PROTECT with varied outcomes.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its attack.
 * @param protectionGain Protection gained when using PROTECT.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record LivingShadow(
        String name,
        int maxHp,
        int attackDamage,
        int protectionGain,
        int expReward) implements EnemyType {

    private static final Random RNG = new Random();

    /**
     * Compact constructor to validate Living Shadow attributes.
     */
    public LivingShadow {
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
     * Default constructor with preset stats for a standard Living Shadow.
     */
    public LivingShadow() {
        this("Living Shadow", 20, 4, 5, 10);
    }

    /**
     * Plans the Living Shadow's action for the turn.
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
     * Returns the texture path for the Living Shadow sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "livingshadow.png";
    }
}
