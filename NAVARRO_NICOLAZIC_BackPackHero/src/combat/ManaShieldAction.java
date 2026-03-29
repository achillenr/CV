package combat;

import main.Combat;
import item.Item;
import item.BackPack;

/**
 * Shield action that consumes mana from connected stones.
 * Applies protection to the hero.
 */
public final class ManaShieldAction implements CombatAction {

    private final int protection;
    private final int energyCost;
    private final int manaCost;
    private final Item shield;

    /**
     * Constructs a ManaShieldAction.
     * 
     * @param protection Base protection value added.
     * @param energyCost Energy cost from the hero.
     * @param manaCost   Mana points required from connected stones.
     * @param shield     The shield item instance.
     */
    public ManaShieldAction(int protection, int energyCost, int manaCost, Item shield) {
        this.protection = protection;
        this.energyCost = energyCost;
        this.manaCost = manaCost;
        this.shield = java.util.Objects.requireNonNull(shield, "shield cannot be null");
    }

    @Override
    public boolean needsTarget() {
        return false; // applies to the hero
    }

    @Override
    public int energyCost() {
        return energyCost;
    }

    /**
     * Executes the defensive action by consuming mana and increasing hero
     * protection.
     * 
     * @param combat        The current combat session.
     * @param ignoredTarget Index is ignored as it targets SELF.
     * @return true if mana was available and protection was increased.
     */
    @Override
    public boolean execute(Combat combat, int ignoredTarget) {
        BackPack backpack = combat.getHero().getBackPack();

        // Consume mana from connected stones; fail if not enough
        if (!backpack.consumeConnectedMana(shield, manaCost)) {
            return false;
        }

        combat.getHero().increaseProtection(protection);
        return true;
    }
}
