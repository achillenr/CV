package item;

/**
 * Defines the various types of consumable items and their primary effects.
 * Each type includes a display name and a brief description of its function.
 */
public enum ConsumableType {
    /** Restores health points (HP) to the hero. */
    HEALTH("Health Potion", "Restores HP"),
    /** Restores energy points used for combat actions. */
    ENERGY("Energy Potion", "Restores Energy"),
    /** Grants immediate temporary protection points. */
    PROTECTION("Protection Scroll", "Grants Protection"),
    /** Restores mana points for magical items. */
    MANA("Mana Potion", "Restores Mana");

    private final String displayName;
    private final String description;

    /**
     * Internal constructor for consumable types.
     * 
     * @param displayName The user-facing name.
     * @param description Brief explanation of the effect.
     */
    ConsumableType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the natural name of the consumable.
     * 
     * @return Display name string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a description of what the item does.
     * 
     * @return Description string.
     */
    public String getDescription() {
        return description;
    }
}
