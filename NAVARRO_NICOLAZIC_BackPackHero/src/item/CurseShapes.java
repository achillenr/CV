package item;

import java.util.List;
import java.util.Random;

/**
 * Utility class providing predefined awkward shapes for curse items.
 * These shapes are designed to clutter the backpack and cannot be moved or
 * rotated by the player.
 */
public final class CurseShapes {

        private static final Random R = new Random();

        /**
         * Internal list of predefined curse shapes ranging from 1x1 to complex 2x2 or
         * 3x1 configurations.
         */
        private static final List<Shape> SHAPES = List.of(

                        // (1x1)
                        Shape.rectangle(1, 1),

                        // (2x1)
                        Shape.rectangle(2, 1),

                        // (1x2)
                        Shape.rectangle(1, 2),

                        // (L-shape)
                        new Shape(new boolean[][] {
                                        { true, true },
                                        { true, false }
                        }),

                        // (Mirrored L-shape)
                        new Shape(new boolean[][] {
                                        { true, false },
                                        { true, true }
                        }),

                        // (2x2 square)
                        Shape.rectangle(2, 2),

                        // (3x1 line)
                        Shape.rectangle(3, 1),

                        // (1x3 column)
                        Shape.rectangle(1, 3));

        /** Private constructor to prevent instantiation of utility class. */
        private CurseShapes() {
        }

        /**
         * Returns a random curse shape from the internal library.
         * 
         * @return A random Shape instance.
         */
        public static Shape random() {
                return SHAPES.get(R.nextInt(SHAPES.size()));
        }
}
