package map;

import java.util.Objects;
import hero.Hero;

/**
 * Represents a non-player character that provides healing services to the hero.
 * Healers can fully restore the hero's health pool.
 * 
 * @param h The Hero instance associated with this healer interaction.
 */
public record Healer(Hero h) {

    /**
     * Compact constructor to validate healer properties.
     * 
     * @param h The hero to be healed.
     * @throws NullPointerException if the hero instance is null.
     */
    public Healer {
        Objects.requireNonNull(h, "Hero instance cannot be null");
    }

    /**
     * Restores the hero's current health points to their maximum capacity.
     */
    public void heal() {
        this.h.setMaxHp();
    }
}
