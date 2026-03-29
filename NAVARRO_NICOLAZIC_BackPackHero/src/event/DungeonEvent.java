package event;

import java.util.List;

/**
 * Interface representing a narrative event encountered in the dungeon.
 * Events have a title, a body description, and a list of possible choices.
 */
public interface DungeonEvent {
    /**
     * Returns the title of the event.
     * 
     * @return The displayable title string.
     */
    String getTitle();

    /**
     * Returns the body text/description of the event.
     * 
     * @return The narrative text string.
     */
    String getBody();

    /**
     * Returns the list of choices available to the player for this event.
     * 
     * @return A List of EventChoice instances.
     */
    List<EventChoice> getChoices();
}
