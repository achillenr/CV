package combat;

/**
 * Enumeration of possible status effects that can be applied to heroes or
 * enemies.
 * Each status has a display name and a description template for tooltips.
 */
public enum Status {
    /** Increases block from defensive items. */
    HASTE("Haste", "Increases block from defensive items by %d."),
    /** Decreases block from defensive items. */
    SLOW("Slow", "Decreases block from defensive items by %d."),
    /** Increases weapon damage. */
    RAGE("Rage", "Increases weapon damage by %d."),
    /** Reduces weapon damage. */
    WEAK("Weakness", "Reduces weapon damage by %d."),

    /** Deals direct damage at the end of the turn (ignores armor/dodge). */
    POISON("Poison", "Deals %d direct damage at the end of the turn (ignores armor/dodge)."),
    /** Deals damage at the start of the turn. */
    BURN("Burn", "Deals %d damage at the start of the turn."),

    /** Ignores a specific number of incoming attacks. */
    DODGE("Dodge", "Ignores the next %d attacks."),
    /** Restores HP at the start of the turn. */
    REGENERATION("Regeneration", "Restores %d HP at the start of the turn."),
    /** Reduces damage from projectiles by 50%. */
    TOUGH_HIDE("Tough Hide", "Reduces damage from projectiles by 50%."),

    /** Blocks a specific amount of incoming damage. */
    BLOCK("Armor", "Blocks the next %d damage."),
    /** Deals damage back to attackers. */
    SPIKES("Spikes", "Deals %d damage to the attacker."),
    /** Chance for enemies to skip their attack. */
    CHARM("Charm", "Enemies have a %d%% chance not to attack."),
    /** Causes the target to take extra damage. */
    CURSE_STATUS("Cursed", "Takes %d extra damage.");

    private final String displayName;
    private final String descriptionTemplate;

    /**
     * Constructs a Status enum element.
     * 
     * @param displayName         The name to display in the UI.
     * @param descriptionTemplate The description template used for tooltips.
     */
    Status(String displayName, String descriptionTemplate) {
        this.displayName = displayName;
        this.descriptionTemplate = descriptionTemplate;
    }

    /**
     * Returns the name of the status effect for display.
     * 
     * @return The displayable name string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Generates a formatted description for the status effect based on its current
     * stack amount.
     * 
     * @param amount The current stack/amount of the status.
     * @return A formatted string describing the effect's current state.
     */
    public String getTooltip(int amount) {
        if (this == TOUGH_HIDE)
            return descriptionTemplate;
        return String.format(descriptionTemplate, amount);
    }
}
