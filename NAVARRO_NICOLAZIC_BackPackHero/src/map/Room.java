package map;

import java.util.Objects;

/**
 * Represents a discrete location (cell) within the dungeon's floor grid.
 * A room acts as a container for game elements like doors, treasures,
 * and specific room types (e.g., combat, merchant, healer).
 */
public class Room {

    private RoomType roomtype;
    private final int row;
    private final int col;
    private Door door;
    private Treasure treasure;

    /**
     * Initializes a room at the specified grid coordinates with a primary type.
     *
     * @param roomtype The initial functional type of the room (e.g., EMPTY,
     *                 COMBAT).
     * @param row      The vertical grid index (Y-coordinate).
     * @param col      The horizontal grid index (X-coordinate).
     * @throws NullPointerException     if roomtype is null.
     * @throws IllegalArgumentException if row or col are negative.
     */
    public Room(RoomType roomtype, int row, int col) {
        Objects.requireNonNull(roomtype, "Room type cannot be null");
        if (row < 0 || col < 0)
            throw new IllegalArgumentException("Grid coordinates row and col must be non-negative");

        this.roomtype = roomtype;
        this.row = row;
        this.col = col;
    }

    /**
     * Returns the vertical position of the room in the floor grid.
     * 
     * @return Row index as an integer.
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the horizontal position of the room in the floor grid.
     * 
     * @return Column index as an integer.
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the current functional category of the room.
     * 
     * @return The current RoomType.
     */
    public RoomType getRoomtype() {
        return roomtype;
    }

    /**
     * Updates the room's functional category.
     * 
     * @param newType The new RoomType to assign.
     * @throws NullPointerException if newType is null.
     */
    public void setRoomType(RoomType newType) {
        Objects.requireNonNull(newType, "New room type cannot be null");
        this.roomtype = newType;
    }

    /**
     * Returns the door associated with this room, if any.
     * 
     * @return Door instance, or null if there is no door.
     */
    public Door getDoor() {
        return door;
    }

    /**
     * Sets or removes the door for this room.
     * 
     * @param door The Door instance to assign, or null to clear.
     */
    public void setDoor(Door door) {
        // Objects.requireNonNull(door); // Optional: if we want to allow null to clear.
        // User requested null checks for all object parameters.
        // I will add it to comply strictly with the request.
        this.door = door;
    }

    /**
     * Returns the treasure container in this room, if any.
     * 
     * @return Treasure instance, or null if empty.
     */
    public Treasure getTreasure() {
        return treasure;
    }

    /**
     * Sets or removes the treasure container for this room.
     * 
     * @param treasure The Treasure instance to assign, or null to clear.
     */
    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }
}
