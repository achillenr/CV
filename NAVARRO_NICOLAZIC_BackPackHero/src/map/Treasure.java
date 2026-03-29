package map;

/**
 * Represents a lootable container located within a dungeon room.
 * Treasures can contain items or gold and track whether they have been looted.
 * 
 * @param id       Unique identifier for the treasure, usually derived from grid
 *                 position.
 * @param isOpened Tracks whether the player has already interactions with the
 *                 treasure.
 */
public record Treasure(int id, boolean isOpened) {

    /**
     * Compact constructor to validate treasure attributes.
     *
     * @param id       Unique identifier.
     * @param isOpened True if the treasure has been looted.
     * @throws IllegalArgumentException if id is negative.
     */
    public Treasure {
        if (id < 0) {
            throw new IllegalArgumentException("Treasure ID cannot be negative");
        }
    }
}
