package item;

import java.util.Objects;

/**
 * Represents the 2D spatial occupation of an item within a grid-based backpack.
 * Defined by a boolean matrix where true indicates a cell is occupied by the
 * item part.
 * 
 * @param matrix The occupancy matrix. Columns represent X indices, rows
 *               represent Y indices.
 */
public record Shape(boolean[][] matrix) {
    /**
     * Compact constructor to normalize potentially jagged input matrices into a
     * rectangular 2D boolean grid and validate its non-emptiness.
     */
    public Shape {
        Objects.requireNonNull(matrix);
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Shape cannot be empty");
        }

        // Calculate max height to normalize jagged arrays
        int maxH = 0;
        for (boolean[] col : matrix) {
            if (col != null) {
                maxH = Math.max(maxH, col.length);
            }
        }

        if (maxH == 0) {
            throw new IllegalArgumentException("Shape cannot be empty");
        }

        // Normalize to rectangular matrix
        int w = matrix.length;
        boolean[][] normalized = new boolean[w][maxH];
        for (int i = 0; i < w; i++) {
            if (matrix[i] != null) {
                for (int j = 0; j < matrix[i].length; j++) {
                    normalized[i][j] = matrix[i][j];
                }
            }
        }

        matrix = normalized;
    }

    /**
     * Returns the horizontal span (width) of the shape.
     * 
     * @return Number of columns.
     */
    public int width() {
        return matrix.length;
    }

    /**
     * Returns the vertical span (height) of the shape.
     * 
     * @return Number of rows.
     */
    public int height() {
        return matrix[0].length;
    }

    /**
     * Checks if the cell at the relative grid coordinates (x, y) is occupied by the
     * item.
     * 
     * @param x Relative X coordinate.
     * @param y Relative Y coordinate.
     * @return true if occupied, false otherwise (including out of bounds).
     */
    public boolean get(int x, int y) {
        if (x < 0 || x >= width() || y < 0 || y >= height()) {
            return false;
        }
        return matrix[x][y];
    }

    /**
     * Creates a new Shape instance representing a 90-degree clockwise rotation.
     * Used for orienting weapons and armor in the backpack.
     * 
     * @return A new rotated Shape.
     */
    public Shape rotate() {
        int w = width();
        int h = height();
        boolean[][] newMatrix = new boolean[h][w];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                newMatrix[j][w - 1 - i] = matrix[i][j];
            }
        }
        return new Shape(newMatrix);
    }

    /**
     * Factory method to create a solid rectangular block shape.
     * 
     * @param w Desired width.
     * @param h Desired height.
     * @return A solid rectangular Shape.
     */
    public static Shape rectangle(int w, int h) {
        boolean[][] m = new boolean[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                m[i][j] = true;
            }
        }
        return new Shape(m);
    }
}
