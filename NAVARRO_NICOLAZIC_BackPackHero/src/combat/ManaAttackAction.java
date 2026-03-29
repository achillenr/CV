package combat;

import main.Combat;
import item.Item;
import enemy.EnemyState;
import item.BackPack;

/**
 * Attack action that consumes mana from connected items (stones).
 * Typically used by magical weapons that require external mana sources.
 */
public final class ManaAttackAction implements CombatAction {

    private final int damage;
    private final int energyCost;
    private final int manaCost;
    private final Item weapon;

    /**
     * Constructs a ManaAttackAction.
     * 
     * @param damage     Base damage to deal.
     * @param energyCost Energy cost from the hero.
     * @param manaCost   Mana cost to be consumed from connected mana stones.
     * @param weapon     The weapon item instance initiating the attack.
     */
    public ManaAttackAction(int damage, int energyCost, int manaCost, Item weapon) {
        this.damage = damage;
        this.energyCost = energyCost;
        this.manaCost = manaCost;
        this.weapon = java.util.Objects.requireNonNull(weapon, "weapon cannot be null");
    }

    @Override
    public boolean needsTarget() {
        return true;
    }

    @Override
    public int energyCost() {
        return energyCost;
    }

    /**
     * Executes the attack by consuming mana and damaging the target.
     * 
     * @param combat      The current combat session.
     * @param targetIndex Index of the target enemy.
     * @return true if mana was available and the attack was executed.
     */
    @Override
    public boolean execute(Combat combat, int targetIndex) {
        BackPack backpack = combat.getHero().getBackPack();

        // Consume mana from stones connected to the weapon
        if (!backpack.consumeConnectedMana(weapon, manaCost)) {
            return false;
        }

        EnemyState enemy = combat.getEnemies().get(targetIndex);
        enemy.receiveDamage(damage);
        return true;
    }
}
