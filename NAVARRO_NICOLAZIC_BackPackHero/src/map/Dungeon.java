package map;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a full game world structure, composed of a sequential list of
 * floors.
 * Manages the chronological progression of the hero through various dungeon
 * levels.
 */
public class Dungeon {

    private final List<Floor> floors;

    /**
     * Initializes an empty dungeon with no floors.
     */
    public Dungeon() {
        this.floors = new ArrayList<>();
    }

    /**
     * Appends a new floor to the end of the dungeon's floor list.
     * 
     * @param floor The Floor instance to add; null values are silently ignored.
     */
    public void addFloor(Floor floor) {
        Objects.requireNonNull(floor, "floor cannot be null");
        this.floors.add(floor);
    }

    /**
     * Retrieves a specific floor based on its sequence index.
     * 
     * @param index The 0-based index of the floor to retrieve.
     * @return The requested Floor instance.
     * @throws IndexOutOfBoundsException if the index is negative or exceeds the
     *                                   current floor count.
     */
    public Floor getFloor(int index) {
        if (index < 0 || index >= floors.size()) {
            throw new IndexOutOfBoundsException("Invalid floor index: " + index);
        }
        return this.floors.get(index);
    }

    /**
     * Returns the total number of floors currently generated in the dungeon.
     * 
     * @return The size of the floor list.
     */
    public int getNumberOfFloors() {
        return this.floors.size();
    }
}
