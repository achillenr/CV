package combat;

import main.Combat;

/**
 * Represents an action in combat.
 * Actions may or may not require a target.
 */
public interface CombatAction {

    /** Returns true if the action requires a target (enemy) */
    boolean needsTarget();

    /** Returns the energy cost to execute this action */
    int energyCost();

    /**
     * Executes the action in the context of a combat.
     *
     * @param combat      The combat instance
     * @param targetIndex Index of the target (ignored if no target needed)
     * @return true if the action was successful
     */
    boolean execute(Combat combat, int targetIndex);
}
