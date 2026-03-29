package combat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages status effects for a combat entity (Hero or Enemy).
 * Tracks stack counts for various effects and handles their modification.
 */
public class StatusManager {

    private final Map<Status, Integer> statuses = new HashMap<>();

    /**
     * Default constructor for StatusManager.
     */
    public StatusManager() {
    }

    /**
     * Adds an amount of stacks to a status effect.
     * Uses merge to sum values if the status already exists.
     * 
     * @param status The status effect to add.
     * @param amount The number of stacks to add (must be > 0).
     */
    public void addStatus(Status status, int amount) {
        Objects.requireNonNull(status, "status cannot be null");
        if (amount <= 0)
            return;
        statuses.merge(status, amount, Integer::sum);
    }

    /**
     * Gets the current level/stacks of a status effect.
     * 
     * @param status The status effect to query.
     * @return The number of stacks currently active (0 if none).
     */
    public int getStatus(Status status) {
        Objects.requireNonNull(status, "status cannot be null");
        return statuses.getOrDefault(status, 0);
    }

    /**
     * Removes a certain amount of stacks from a status effect.
     * If the stack count reaches zero or less, the status is removed from the
     * active map.
     * 
     * @param status The status effect to modify.
     * @param amount The number of stacks to decrement.
     */
    public void removeStatus(Status status, int amount) {
        Objects.requireNonNull(status, "status cannot be null");
        if (!statuses.containsKey(status))
            return;
        int current = statuses.get(status);
        int newVal = current - amount;
        if (newVal <= 0) {
            statuses.remove(status);
        } else {
            statuses.put(status, newVal);
        }
    }

    /**
     * Completely removes a status effect, regardless of its stack count.
     * 
     * @param status The status effect to clear.
     */
    public void clearStatus(Status status) {
        Objects.requireNonNull(status, "status cannot be null");
        statuses.remove(status);
    }

    /**
     * Returns a copy of the internal status map containing all active effects.
     * 
     * @return A Map of active Status types to their respective stack counts.
     */
    public Map<Status, Integer> getAllStatuses() {
        return new HashMap<>(statuses);
    }
}
