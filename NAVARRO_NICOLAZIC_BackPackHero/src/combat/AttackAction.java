package combat;

import enemy.EnemyState;
import main.Combat;

/**
 * Standard melee or physical attack targeting an enemy.
 */
public final class AttackAction implements CombatAction {

    private final int damage;
    private final int energyCost;
    private final java.util.Map<Status, Integer> effects;

    /**
     * Constructs an AttackAction.
     * 
     * @param damage     Base damage value.
     * @param energyCost Energy cost for the attack.
     * @param effects    Status effects applied to the enemy.
     */
    public AttackAction(int damage, int energyCost, java.util.Map<Status, Integer> effects) {
        this.damage = damage;
        this.energyCost = energyCost;
        this.effects = java.util.Objects.requireNonNullElse(effects, java.util.Map.of());
    }

    /**
     * Simplified constructor for AttackAction without status effects.
     */
    public AttackAction(int damage, int energyCost) {
        this(damage, energyCost, java.util.Map.of());
    }

    @Override
    public boolean needsTarget() {
        return true;
    }

    @Override
    public int energyCost() {
        return energyCost;
    }

    @Override
    public boolean execute(Combat combat, int targetIndex) {
        if (targetIndex < 0 || targetIndex >= combat.getEnemies().size()) {
            return false;
        }

        EnemyState enemy = combat.getEnemies().get(targetIndex);
        if (!enemy.isAlive())
            return false;

        // Apply Attacker (Hero) Statuses
        combat.StatusManager heroStatus = combat.getHero().getStatusManager();
        int rage = heroStatus.getStatus(Status.RAGE);
        int weak = heroStatus.getStatus(Status.WEAK);

        int finalDamage = damage + rage - weak;
        if (finalDamage < 0)
            finalDamage = 0;

        enemy.receiveDamage(finalDamage);

        // Apply Weapon Effects
        if (!effects.isEmpty()) {
            effects.forEach((status, amount) -> {
                enemy.getStatusManager().addStatus(status, amount);
            });
        }

        // Apply Defender (Enemy) Statuses - SPIKES
        combat.StatusManager enemyStatus = enemy.getStatusManager();
        int spikes = enemyStatus.getStatus(Status.SPIKES);
        if (spikes > 0) {
            combat.getHero().receiveDamage(spikes);
        }

        return true;
    }
}
