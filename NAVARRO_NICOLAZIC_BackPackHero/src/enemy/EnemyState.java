package enemy;

import java.util.List;
import java.util.Objects;

/**
 * Represents the current state of an enemy during combat.
 * Holds its current HP, protection, active status effects, and base type.
 */
public class EnemyState {

    private final EnemyType type;
    private int hp;
    private int protection;
    private final combat.StatusManager statusManager = new combat.StatusManager();

    /**
     * Constructs a new EnemyState for a specific EnemyType.
     * 
     * @param type The template for this enemy.
     * @throws NullPointerException if type is null.
     */
    public EnemyState(EnemyType type) {
        this.type = Objects.requireNonNull(type, "EnemyType is null");

        EnemyType.validate(type);

        this.hp = type.maxHp();
        this.protection = 0;
    }

    /**
     * Checks if the enemy is still alive (HP > 0).
     * 
     * @return true if HP is greater than zero.
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Returns the name of the enemy type.
     * 
     * @return String name.
     */
    public String getName() {
        return type.name();
    }

    /**
     * Returns the enemy's current hit points.
     * 
     * @return Current HP value.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Returns the enemy's maximum hit points as defined by its type.
     * 
     * @return Max HP value.
     */
    public int getMaxHp() {
        return type.maxHp();
    }

    /**
     * Returns the current protection status.
     * 
     * @return Current protection points.
     */
    public int getProtection() {
        return protection;
    }

    /**
     * Returns the base attack damage of this enemy type.
     * 
     * @return Attack damage.
     */
    public int getAttackDamage() {
        return type.attackDamage();
    }

    /**
     * Returns the base amount of protection gained from a PROTECT action.
     * 
     * @return Protection gain value.
     */
    public int getProtectionGain() {
        return type.protectionGain();
    }

    /**
     * Returns the experience points granted when this enemy is defeated.
     * 
     * @return EXP reward value.
     */
    public int getExpReward() {
        return type.expReward();
    }

    /**
     * Returns the underlying EnemyType instance.
     * 
     * @return The EnemyType template.
     */
    public EnemyType getType() {
        return type;
    }

    /**
     * Resets the enemy's protection points to zero.
     */
    public void resetProtection() {
        protection = 0;
    }

    /**
     * Increases protection points based on the enemy type's base gain and current
     * HASTE/SLOW statuses.
     */
    public void increaseProtection() {
        int base = type.protectionGain();
        int haste = statusManager.getStatus(combat.Status.HASTE);
        int slow = statusManager.getStatus(combat.Status.SLOW);

        int finalAmt = base + haste - slow;
        if (finalAmt < 0)
            finalAmt = 0;

        protection += finalAmt;
    }

    /**
     * Applies damage to the enemy.
     * Protection is consumed before HP. Dodging can negate all damage.
     *
     * @param damage The amount of damage to apply (must be positive).
     * @throws IllegalArgumentException if damage is negative.
     */
    public void receiveDamage(int damage) {
        if (damage < 0)
            throw new IllegalArgumentException("Negative damage not allowed");

        if (checkDodge())
            return;

        int absorbed = Math.min(damage, protection);
        hp = Math.max(0, hp - (damage - absorbed));
        protection -= absorbed;
    }

    /**
     * Checks if the enemy can dodge an incoming attack using its DODGE status.
     * 
     * @return true if the attack was dodged.
     */
    private boolean checkDodge() {
        if (statusManager.getStatus(combat.Status.DODGE) > 0) {
            statusManager.removeStatus(combat.Status.DODGE, 1);
            return true;
        }
        return false;
    }

    /**
     * Heals the enemy by a given amount, capping at maximum HP.
     * 
     * @param amount The amount to heal.
     */
    public void heal(int amount) {
        if (amount < 0)
            return;
        hp += amount;
        if (hp > type.maxHp())
            hp = type.maxHp();
    }

    /**
     * Applies damage directly to HP, bypassing protection.
     * 
     * @param amount The amount of damage.
     */
    public void receiveDirectDamage(int amount) {
        if (amount < 0)
            return;
        hp -= amount;
        if (hp < 0)
            hp = 0;
    }

    /**
     * Returns the list of actions this enemy type plans to execute.
     * 
     * @return List of EnemyAction.
     */
    public List<EnemyAction> planAction() {
        return type.planAction();
    }

    /**
     * Returns the hero's status manager instance.
     * 
     * @return StatusManager instance.
     */
    public combat.StatusManager getStatusManager() {
        return statusManager;
    }

}
