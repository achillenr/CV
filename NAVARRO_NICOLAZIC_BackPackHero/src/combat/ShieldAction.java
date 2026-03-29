package combat;

import main.Combat;

/**
 * Action that adds protection to the hero using standard energy.
 * Does not target enemies.
 */
public class ShieldAction implements CombatAction {

    private final int protection;
    private final int energyCost;

    /**
     * Constructs a ShieldAction.
     * 
     * @param protection Amount of protection/shield added to the hero.
     * @param energyCost Energy cost to use the shield.
     */
    public ShieldAction(int protection, int energyCost) {
        this.protection = protection;
        this.energyCost = energyCost;
    }

    /**
     * Returns whether this action requires a target.
     * 
     * @return false (Shields target the hero).
     */
    @Override
    public boolean needsTarget() {
        return false;
    }

    /**
     * Returns the energy cost for this action.
     * 
     * @return Energy cost.
     */
    @Override
    public int energyCost() {
        return energyCost;
    }

    /**
     * Executes the action by increasing the hero's protection.
     * 
     * @param combat  The current combat session.
     * @param ignored Target index is ignored as it targets SELF.
     * @return true always (standard execution without extra requirements).
     */
    @Override
    public boolean execute(Combat combat, int ignored) {
        combat.getHero().increaseProtection(protection);
        return true;
    }
}
