package event;

import java.util.function.Consumer;
import display.DungeonView;

/**
 * Represents a single choice within a dungeon event.
 * 
 * @param text   The label shown on the choice button.
 * @param action The callback executed when the choice is selected, taking the
 *               current DungeonView as context.
 */
public record EventChoice(String text, Consumer<DungeonView> action) {
    public EventChoice {
        java.util.Objects.requireNonNull(text, "text cannot be null");
        java.util.Objects.requireNonNull(action, "action cannot be null");
    }
}
