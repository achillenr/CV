package enemy;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents the Bee Queen enemy.
 * A powerful insectoid foe that balances aggression and defense.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its swarm attack.
 * @param protectionGain Protection gained per turn.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record BeeQueen(
        String name,
        int maxHp,
        int attackDamage,
        int protectionGain,
        int expReward) implements EnemyType {

    private static final Random RNG = new Random();

    /**
     * Compact constructor to validate Bee Queen attributes.
     */
    public BeeQueen {
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
     * Default constructor with preset stats for a standard Bee Queen.
     */
    public BeeQueen() {
        this("Bee Queen", 20, 4, 5, 10);
    }

    /**
     * Plans the Bee Queen's action for the turn.
     * Randomly chooses between ATTACK and PROTECT with equal probability.
     *
     * @return A list containing the selected EnemyAction.
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
     * Returns the texture path for the Bee Queen sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "beequeen.png";
    }
}
