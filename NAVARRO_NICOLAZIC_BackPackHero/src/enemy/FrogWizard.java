package enemy;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a Frog Wizard enemy.
 * A magical amphibian that alternates between offensive spells and defensive
 * charms.
 * 
 * @param name           The name of the enemy.
 * @param maxHp          Maximum hit points.
 * @param attackDamage   Damage dealt by its magical blast.
 * @param protectionGain Protection gained when using PROTECT.
 * @param expReward      Experience granted to the hero upon defeat.
 */
public record FrogWizard(
        String name,
        int maxHp,
        int attackDamage,
        int protectionGain,
        int expReward) implements EnemyType {

    private static final Random RNG = new Random();

    /**
     * Compact constructor to validate Frog Wizard attributes.
     */
    public FrogWizard {
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
     * Default constructor with preset stats for a standard Frog Wizard.
     */
    public FrogWizard() {
        this("Frog Wizard", 20, 4, 5, 10);
    }

    /**
     * Plans the Frog Wizard's action for the turn.
     * Randomly decides whether to cast an ATTACK spell or apply a PROTECT shield.
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
     * Returns the texture path for the Frog Wizard sprite.
     * 
     * @return Path to the image resource.
     */
    @Override
    public String getTexturePath() {
        return "frogwitch.png";
    }
}
