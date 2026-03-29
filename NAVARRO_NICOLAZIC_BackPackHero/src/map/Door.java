package map;

/**
 * Represents a physical gateway between rooms in the dungeon.
 * Doors can be in an open or closed state and are uniquely identified.
 * 
 * @param id     A unique identifier for the door, typically derived from its
 *               grid coordinates.
 * @param isOpen The initial traversal state of the door.
 */
public record Door(int id, boolean isOpen) {

    /**
     * Compact constructor to validate door attributes.
     *
     * @param id     Unique identifier.
     * @param isOpen True if the door is currently unlocked/open.
     * @throws IllegalArgumentException if id is negative.
     */
    public Door(int id, boolean isOpen) {
        if (id < 0)
            throw new IllegalArgumentException("Door ID cannot be negative");
        this.id = id;
        this.isOpen = isOpen;
    }
}
