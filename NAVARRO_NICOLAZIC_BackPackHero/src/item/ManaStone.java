package item;

import java.util.Optional;

import combat.CombatAction;
import combat.ManaAttackAction;

/**
 * Represents a magical stone that acts as a container for mana.
 * Mana stones provide resources for mana-based weapons and items.
 * They can be refilled and their current mana is displayed as an overlay in the
 * UI.
 */
public non-sealed class ManaStone implements Item {
    private final int id;
    private final int energyCost;
    private final int manaCost;
    private final String name;
    private final Rarity rarity;
    private final Shape shape;
    private final int maxMana;
    private int currentMana;
    private boolean isSellable;

    /**
     * Initializes a new ManaStone with specified properties.
     * 
     * @param id          Unique identifier for the item.
     * @param name        Display name of the stone.
     * @param mana        Maximum mana capacity.
     * @param isSellable  Whether the stone can be sold.
     * @param energyCost  Energy cost to manipulate the stone in combat.
     * @param rarity      Rarity affecting its value and appearance.
     * @param shape       2D grid shape in the backpack.
     * @param currentMana The initial current mana level.
     */
    public ManaStone(int id, String name, int mana, boolean isSellable, int energyCost, Rarity rarity, Shape shape,
            int currentMana) {
        this.id = id;
        this.manaCost = 0; // Mana stones don't typically cost mana to use themselves
        this.name = java.util.Objects.requireNonNull(name, "name cannot be null");
        this.maxMana = mana;
        this.currentMana = currentMana;
        this.energyCost = energyCost;
        this.rarity = java.util.Objects.requireNonNull(rarity, "rarity cannot be null");
        this.shape = java.util.Objects.requireNonNull(shape, "shape cannot be null");
        this.isSellable = isSellable;
    }

    /**
     * Returns the current mana level of the stone.
     * 
     * @return Current mana units.
     */
    public int currentMana() {
        return currentMana;
    }

    /**
     * Returns the maximum mana capacity of the stone.
     * 
     * @return Max mana units.
     */
    public int mana() {
        return maxMana;
    }

    /**
     * Consumes a specified amount of mana from the stone.
     * 
     * @param amount Amount to subtract.
     */
    public void consume(int amount) {
        this.currentMana = Math.max(0, currentMana - amount);
    }

    /**
     * Refills the mana stone to its maximum capacity.
     */
    public void refill() {
        this.currentMana = maxMana;
    }

    /**
     * Returns the resale value of the mana stone based on its capacity.
     * 
     * @return Value in gold.
     */
    @Override
    public int getValue() {
        return maxMana;
    }

    /**
     * Generates a detailed tooltip description of the stone's mana stats.
     * 
     * @return A formatted string for UI display.
     */
    @Override
    public String getStatDescription() {
        return "Mana: " + maxMana + "\nEnergy: " + energyCost;
    }

    /**
     * Creates a new ManaStone instance with a different orientation/shape.
     * 
     * @param shape The new Shape to apply.
     * @return A new ManaStone instance.
     */
    @Override
    public Item withShape(Shape shape) {
        return new ManaStone(id, name, maxMana, isSellable, energyCost, rarity, shape, currentMana);
    }

    /**
     * Returns the combat action associated with this stone (usually a minor magical
     * attack or resource manipulation).
     * 
     * @return An Optional containing a ManaAttackAction.
     */
    @Override
    public Optional<CombatAction> combatAction() {
        return Optional.of(new ManaAttackAction(5, 0, 1, this));
    }

    /**
     * Returns the unique identifier of the item.
     * 
     * @return The ID.
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * Returns whether the item can be sold.
     * 
     * @return true if sellable.
     */
    @Override
    public boolean isSellable() {
        return isSellable;
    }

    /**
     * Returns the display name of the item.
     * 
     * @return The name.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Returns the rarity grade of the item.
     * 
     * @return The Rarity.
     */
    @Override
    public Rarity rarity() {
        return rarity;
    }

    /**
     * Returns the 2D shape of the item in the backpack.
     * 
     * @return The Shape.
     */
    @Override
    public Shape shape() {
        return shape;
    }

    /**
     * Returns the energy cost of selecting/using this item.
     * 
     * @return The energy cost.
     */
    @Override
    public int energyCost() {
        return energyCost;
    }

    /**
     * Returns the file path to this mana stone's texture sprite.
     * 
     * @return Resource path string.
     */
    @Override
    public String getTexturePath() {
        return "manastone.png";
    }

    /**
     * Returns the inherent mana cost (usually 0 for the stone itself).
     * 
     * @return Mana cost.
     */
    @Override
    public int manaCost() {
        return manaCost;
    }

    /**
     * Utility check to identify ManaStone instances.
     * 
     * @return true.
     */
    @Override
    public boolean isManaStone() {
        return true;
    }

    /**
     * Safely casts this item to a ManaStone.
     * 
     * @return This instance.
     */
    @Override
    public ManaStone asManaStone() {
        return this;
    }

    /**
     * Renders a textual overlay showing "current/max" mana on the backpack slot.
     * 
     * @param g        The graphics context.
     * @param x        Top-left X coordinate.
     * @param y        Top-left Y coordinate.
     * @param cellSize Size of a single grid cell.
     */
    @Override
    public void renderOverlay(java.awt.Graphics2D g, int x, int y, int cellSize) {
        int fontSize = Math.max(10, (shape().height() * cellSize) / 3);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, fontSize));

        String text = currentMana + "/" + maxMana;
        int textX = x + (shape().width() * cellSize) / 2 - g.getFontMetrics().stringWidth(text) / 2;
        int textY = y + (shape().height() * cellSize) / 2 + g.getFontMetrics().getAscent() / 2;

        g.setColor(java.awt.Color.BLACK);
        g.drawString(text, textX + 1, textY + 1);
        g.setColor(java.awt.Color.WHITE);
        g.drawString(text, textX, textY);
    }
}
