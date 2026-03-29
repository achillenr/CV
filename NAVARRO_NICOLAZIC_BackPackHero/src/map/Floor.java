package map;

import java.util.Objects;

/**
 * Represents a single level within the dungeon, organized as a 2D grid of Room
 * objects.
 * Handles the storage and retrieval of rooms based on their grid coordinates.
 */
public class Floor {

    private final Room[][] rooms;
    private final int rows;
    private final int cols;

    /** Default number of vertical rooms in a floor. */
    private static final int DEFAULT_ROWS = 5;
    /** Default number of horizontal rooms in a floor. */
    private static final int DEFAULT_COLS = 11;

    /**
     * Initializes a floor using a pre-existing 2D array of rooms.
     *
     * @param initialRooms A 2D array representing the floor's grid layout.
     * @throws NullPointerException     if initialRooms is null.
     * @throws IllegalArgumentException if initialRooms is empty or has zero-length
     *                                  rows.
     */
    Floor(Room[][] initialRooms) {
        Objects.requireNonNull(initialRooms, "initialRooms should not be null.");
        if (initialRooms.length == 0 || initialRooms[0].length == 0) {
            throw new IllegalArgumentException("initial rooms should not be empty");
        }

        this.rooms = initialRooms;
        this.rows = initialRooms.length;
        this.cols = initialRooms[0].length;
    }

    /**
     * Factory method that creates a floor with the default size (5x11).
     * 
     * @return A new Floor instance.
     */
    public static Floor createDefaultFloor() {
        return InitFloor.createFloor(DEFAULT_ROWS, DEFAULT_COLS);
    }

    /**
     * Retrieves the room located at the specified grid coordinates.
     *
     * @param row The vertical index (Y-coordinate).
     * @param col The horizontal index (X-coordinate).
     * @return The Room instance at the given location.
     * @throws IllegalArgumentException if the coordinates are outside the floor's
     *                                  dimensions.
     */
    public Room getRoom(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IllegalArgumentException("Requested coordinates are outside the floor boundaries.");
        }
        return this.rooms[row][col];
    }

    /**
     * Returns the total number of rows (height) of this floor.
     * 
     * @return Row count.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the total number of columns (width) of this floor.
     * 
     * @return Column count.
     */
    public int getCols() {
        return cols;
    }
}
