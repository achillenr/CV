package enemy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Provides static access to a pool of available enemy types and handles random
 * generation.
 * New enemy types must be registered in the static block.
 */
public class EnemyList {

    private static final Random r = new Random();

    private static final List<EnemyType> ENEMY_TYPES = new ArrayList<>();

    static {
        ENEMY_TYPES.add(new SmallWolfRat());
        ENEMY_TYPES.add(new WolfRat());
        ENEMY_TYPES.add(new CombatTurtle());
        ENEMY_TYPES.add(new CursedSpecter());
        ENEMY_TYPES.add(new Goblin());
        ENEMY_TYPES.add(new LivingShadow());
        ENEMY_TYPES.add(new BeeQueen());
        ENEMY_TYPES.add(new FrogWizard());
    }

    /**
     * Randomly selects an EnemyType from the registered list.
     *
     * @return A random EnemyType instance.
     * @throws IllegalStateException if no enemy types are registered.
     */
    public static EnemyType getRandomEnemyType() {
        if (ENEMY_TYPES.isEmpty()) {
            throw new IllegalStateException("No EnemyType registered");
        }
        return ENEMY_TYPES.get(r.nextInt(ENEMY_TYPES.size()));
    }

    /**
     * Creates and initializes a new EnemyState based on a randomly selected
     * EnemyType.
     *
     * @return A newly initialized EnemyState instance.
     */
    public static EnemyState createRandomEnemy() {
        EnemyType type = getRandomEnemyType();
        Objects.requireNonNull(type, "EnemyType is null");
        return new EnemyState(type);
    }
}
