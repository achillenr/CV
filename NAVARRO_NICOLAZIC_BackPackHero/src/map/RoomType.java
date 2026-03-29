package map;

/**
 * Defines the functional characteristics and visual representation of a dungeon
 * room.
 * Each type determines what interactions (combat, healing, trading) are
 * available
 * and which texture sprite should be rendered.
 */
public enum RoomType {
    /** Starting location for the hero on a new floor. */
    START,
    /** Room containing hostile entities and combat initiation. */
    ENEMY,
    /** Trading hub for buying and selling items. */
    MERCHANT,
    /** Sacred site for restoring life force. */
    HEALER,
    /** Unoccupied chamber with no special properties. */
    EMPTY,
    /** Transitory passage linking functional rooms. */
    CORRIDOR,
    /** Obstructed path requiring a key to traverse. */
    DOOR,
    /** Reward chamber containing a lootable chest. */
    TREASURE,
    /** Mystery location triggering a random narrative event. */
    EVENT,
    /** Objective room leading to the subsequent dungeon floor. */
    EXIT;

    /**
     * Returns the relative file system path to the texture sprite for this room
     * category.
     * 
     * @return A resource path string, or null if the type has no unique visual
     *         representation
     *         (e.g., EMPTY or CORRIDOR which use base floor tiles).
     */
    public String getImagePath() {
        return switch (this) {
            case START -> "start.png";
            case ENEMY -> "enemi.png";
            case MERCHANT -> "merchant.png";
            case HEALER -> "healer.png";
            case TREASURE -> "chest.png";
            case EVENT -> "event.png";
            case EXIT -> "exit.png";
            case DOOR -> "door.png";
            default -> null; // EMPTY, CORRIDOR...
        };
    }
}
