package map;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Utility class for generating dungeon floors with rooms, corridors, doors, and
 * treasures. Uses a procedural generation approach to place key rooms and
 * connect
 * them with paths.
 */
public class InitFloor {

    private static final Random RANDOM = new Random();

    /**
     * Generates a fully initialized floor of the specified size.
     * The generation process involves placing start/exit rooms, special content
     * rooms
     * (merchants, healers, etc.), connecting them via corridors, and placing a
     * guardian door safely.
     *
     * @param rows The number of vertical rows in the floor grid.
     * @param cols The number of horizontal columns in the floor grid.
     * @return A fully populated and interconnected Floor instance.
     */
    public static Floor createFloor(int rows, int cols) {
        Room[][] initialRooms = createEmptyRoomGrid(rows, cols);
        Floor floor = new Floor(initialRooms);

        // 1 & 2. Place all necessary rooms
        List<Room> pathRooms = placeRequiredRooms(floor);

        // 3. Connect them
        connectAllRooms(floor, pathRooms);

        // 4. Place exit security
        placeDoorGuardingRoom(floor, pathRooms.get(pathRooms.size() - 1));

        return floor;
    }

    /**
     * Places the standard set of rooms required for a floor.
     */
    private static List<Room> placeRequiredRooms(Floor floor) {
        List<Room> rooms = new ArrayList<>();
        Room start = placeSingleRoom(floor, RoomType.START);
        rooms.add(start);

        rooms.add(placeSingleRoom(floor, RoomType.MERCHANT));
        rooms.add(placeSingleRoom(floor, RoomType.HEALER));
        rooms.add(placeSingleRoom(floor, RoomType.EVENT));
        rooms.addAll(placeMultipleRooms(floor, RoomType.TREASURE, 2));
        rooms.addAll(placeMultipleRooms(floor, RoomType.ENEMY, 3));

        rooms.add(placeSingleRoom(floor, RoomType.EXIT));
        return rooms;
    }

    /**
     * Places a door in a corridor or adjacent room to "guard" a specific target
     * room (usually the exit).
     * 
     * @param floor  The Floor instance where the door should be placed.
     * @param target The Room instance that requires protection.
     */
    private static void placeDoorGuardingRoom(Floor floor, Room target) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Objects.requireNonNull(target, "target cannot be null");
        int r = target.getRow();
        int c = target.getCol();
        int[] dr = { -1, 1, 0, 0 };
        int[] dc = { 0, 0, -1, 1 };

        // Attempt 1: Place on an existing corridor
        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (nr >= 0 && nr < floor.getRows() && nc >= 0 && nc < floor.getCols()) {
                Room n = floor.getRoom(nr, nc);
                if (n.getRoomtype() == RoomType.CORRIDOR) {
                    n.setRoomType(RoomType.DOOR);
                    n.setDoor(new Door(nr * 1000 + nc, false));
                    return;
                }
            }
        }

        // Attempt 2: Override any room that isn't start or exit
        for (int i = 0; i < 4; i++) {
            int nr = r + dr[i];
            int nc = c + dc[i];
            if (nr >= 0 && nr < floor.getRows() && nc >= 0 && nc < floor.getCols()) {
                Room n = floor.getRoom(nr, nc);
                RoomType t = n.getRoomtype();
                if (t != RoomType.START && t != RoomType.EXIT) {
                    n.setRoomType(RoomType.DOOR);
                    n.setDoor(new Door(nr * 1000 + nc, false));
                    return;
                }
            }
        }
    }

    /**
     * Sequentially connects a list of rooms with corridors.
     * 
     * @param floor          The floor grid to modify.
     * @param roomsToConnect Ordered list of rooms to be linked.
     */
    private static void connectAllRooms(Floor floor, List<Room> roomsToConnect) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Objects.requireNonNull(roomsToConnect, "roomsToConnect cannot be null");
        if (roomsToConnect.size() < 2)
            return;

        Room currentRoom = roomsToConnect.get(0);
        for (int i = 1; i < roomsToConnect.size(); i++) {
            Room nextRoom = roomsToConnect.get(i);
            createPath(floor, currentRoom, nextRoom);
            currentRoom = nextRoom;
        }
    }

    /**
     * Carves a rectangular path (corridors) between two room locations.
     * 
     * @param floor The floor grid to modify.
     * @param start The starting Room coordinates.
     * @param end   The target Room coordinates.
     */
    private static void createPath(Floor floor, Room start, Room end) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");
        int r1 = start.getRow();
        int c1 = start.getCol();
        int r2 = end.getRow();
        int c2 = end.getCol();

        int stepC = (c1 < c2) ? 1 : -1;
        for (int c = c1; c != c2; c += stepC) {
            markAsCorridor(floor, r1, c);
        }

        int stepR = (r1 < r2) ? 1 : -1;
        for (int r = r1; r != r2; r += stepR) {
            markAsCorridor(floor, r, c2);
        }
    }

    /**
     * Converts an EMPTY room into a CORRIDOR at the specified location.
     * 
     * @param floor The Floor grid.
     * @param r     Row index.
     * @param c     Column index.
     */
    private static void markAsCorridor(Floor floor, int r, int c) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Room room = floor.getRoom(r, c);
        if (room.getRoomtype() == RoomType.EMPTY) {
            room.setRoomType(RoomType.CORRIDOR);
        }
    }

    /**
     * Generates a structural grid of EMPTY rooms.
     * 
     * @param rows Vertical dimension.
     * @param cols Horizontal dimension.
     * @return A 2D array of Room objects.
     */
    private static Room[][] createEmptyRoomGrid(int rows, int cols) {
        Room[][] initialRooms = new Room[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                initialRooms[i][j] = new Room(RoomType.EMPTY, i, j);
            }
        }
        return initialRooms;
    }

    /**
     * Places a specific room type at a random unoccupied grid coordinate.
     * 
     * @param floor The floor to modify.
     * @param type  The RoomType to instantiate.
     * @return The Room instance that was successfully placed.
     */
    private static Room placeSingleRoom(Floor floor, RoomType type) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        int rows = floor.getRows();
        int cols = floor.getCols();

        while (true) {
            int row = RANDOM.nextInt(rows);
            int col = RANDOM.nextInt(cols);
            Room room = floor.getRoom(row, col);

            if (room.getRoomtype() == RoomType.EMPTY) {
                room.setRoomType(type);
                switch (type) {
                    case DOOR -> room.setDoor(new Door(row * 1000 + col, false));
                    case TREASURE -> room.setTreasure(new Treasure(row * 1000 + col, false));
                    default -> {
                    }
                }
                return room;
            }
        }
    }

    /**
     * Places multiple rooms of a specific type at random unoccupied coordinates.
     * 
     * @param floor The floor to modify.
     * @param type  The RoomType to instantiate.
     * @param count The number of rooms to place.
     * @return A list of successfully placed Room instances.
     */
    private static List<Room> placeMultipleRooms(Floor floor, RoomType type, int count) {
        Objects.requireNonNull(floor, "floor cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        List<Room> placedRooms = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            placedRooms.add(placeSingleRoom(floor, type));
        }
        return placedRooms;
    }
}
