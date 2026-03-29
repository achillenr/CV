package enemy;

/**
 * Actions that an enemy can perform during its turn in combat.
 */
public enum EnemyAction {
    /** The enemy deals damage to the hero. */
    ATTACK,
    /** The enemy gains protection points. */
    PROTECT,
    /** The enemy applies a curse item or status to the hero. */
    CURSE
}
