package combat;

import main.Combat;
import enemy.EnemyState;
import java.util.Objects;

/**
 * Action that applies stacks of a specific status effect to either the caster
 * or an enemy.
 */
public class ApplyStatusAction implements CombatAction {

    /**
     * Defines the target of the status application.
     */
    public enum Target {
        /** The status is applied to the hero. */
        SELF,
        /** The status is applied to a targeted enemy. */
        ENEMY
    }

    private final Target targetType;
    private final Status status;
    private final int amount;
    private final int energyCost;

    /**
     * Constructs an ApplyStatusAction.
     * 
     * @param targetType Who receives the status (SELF or ENEMY).
     * @param status     The status effect to apply.
     * @param amount     The number of stacks to add.
     * @param energyCost The energy cost to perform this action.
     */
    public ApplyStatusAction(Target targetType, Status status, int amount, int energyCost) {
        this.targetType = Objects.requireNonNull(targetType, "targetType cannot be null");
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.amount = amount;
        this.energyCost = energyCost;
    }

    @Override
    public boolean needsTarget() {
        return targetType == Target.ENEMY;
    }

    @Override
    public int energyCost() {
        return energyCost;
    }

    /**
     * Executes the status application.
     * 
     * @param combat      The current combat session.
     * @param targetIndex Index of the target enemy (used only if targetType is
     *                    ENEMY).
     * @return true if the status was successfully applied.
     */
    @Override
    public boolean execute(Combat combat, int targetIndex) {
        if (targetType == Target.SELF) {
            combat.getHero().getStatusManager().addStatus(status, amount);
            return true;
        } else {
            if (targetIndex < 0 || targetIndex >= combat.getEnemies().size()) {
                return false;
            }
            EnemyState enemy = combat.getEnemies().get(targetIndex);
            if (!enemy.isAlive())
                return false;

            enemy.getStatusManager().addStatus(status, amount);
            return true;
        }
    }
}
