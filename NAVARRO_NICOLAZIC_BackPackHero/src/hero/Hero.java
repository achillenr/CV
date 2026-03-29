
package hero;

import java.util.Objects;

/**
 * Represents the hero controlled by the player.
 * Maintains stats, backpack, position, experience, mana, energy, gold, etc.
 */
public class Hero {
    private int maxHp = 40;
    private int hp = 40;
    private int protection = 0;
    private int maxEnergy = 3;
    private int energy = 3;
    private int maxMana = 30;
    private int mana = 30;
    private int level = 1;
    private int exp = 0;
    private int expToLevel = 20;
    private int curseRefusalCount = 0;

    private final item.BackPack backpack = new item.BackPack();
    private final combat.StatusManager statusManager = new combat.StatusManager();
    private int row = -1;
    private int col = -1;

    /**
     * Constructs a new Hero with a default backpack initialization.
     */
    public Hero() {
        backpack.init_backpack();
    }

    /**
     * Returns the hero's backpack.
     * 
     * @return The BackPack instance.
     */
    public item.BackPack getBackPack() {
        return backpack;
    }

    /**
     * Returns the hero's status manager.
     * 
     * @return The StatusManager instance.
     */
    public combat.StatusManager getStatusManager() {
        return statusManager;
    }

    /**
     * Returns the hero's current hit points.
     * 
     * @return Current HP value.
     */
    public int getHp() {
        return hp;
    }

    /**
     * Returns the hero's maximum hit points.
     * 
     * @return Maximum HP value.
     */
    public int getMaxHp() {
        return maxHp;
    }

    /**
     * Returns the hero's current protection value.
     * 
     * @return Protection value.
     */
    public int getProtection() {
        return protection;
    }

    /**
     * Returns the total amount of gold currently held by the hero.
     * Searches through the backpack for Gold items.
     * 
     * @return Total gold amount.
     */
    public int getGold() {
        // Search for Gold item in backpack
        for (item.Item item : backpack.getItems()) {
            switch (item) {
                case item.Gold gold -> {
                    return gold.amount();
                }
                default -> {
                }
            }
        }
        return 0;
    }

    /**
     * Returns the hero's current energy.
     * 
     * @return Current energy value.
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Sets current energy within valid bounds [0, maxEnergy].
     * 
     * @param e New energy value.
     */
    public void setEnergy(int e) {
        energy = Math.min(Math.max(e, 0), maxEnergy);
    }

    /**
     * Returns the hero's maximum energy.
     * 
     * @return Maximum energy value.
     */
    public int getMaxEnergy() {
        return maxEnergy;
    }

    /**
     * Returns the hero's current mana.
     * 
     * @return Current mana value.
     */
    public int getMana() {
        return mana;
    }

    /**
     * Sets current mana within valid bounds [0, maxMana].
     * 
     * @param m New mana value.
     */
    public void setMana(int m) {
        mana = Math.min(Math.max(m, 0), maxMana);
    }

    /**
     * Restores hero's HP to its maximum value.
     */
    public void setMaxHp() {
        hp = maxHp;
    }

    /**
     * Increases the hero's maximum HP and heals them by the same amount.
     * 
     * @param amount The amount to increase by.
     */
    public void addMaxHp(int amount) {
        maxHp += amount;
        hp += amount;
    }

    /**
     * Returns information about maximum mana.
     * 
     * @return Maximum mana value.
     */
    public int getMaxMana() {
        return maxMana;
    }

    /**
     * Returns current experience points.
     * 
     * @return Current EXP.
     */
    public int getExp() {
        return exp;
    }

    /**
     * Returns the hero's current level.
     * 
     * @return Current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the experience required to reach the next level.
     * 
     * @return Required EXP.
     */
    public int getExpToLevel() {
        return expToLevel;
    }

    /**
     * Reduces HP by the incoming damage, after applying protection.
     * Protection is consumed before HP.
     *
     * @param dmg Amount of damage to apply.
     * @throws IllegalArgumentException if dmg is negative.
     */
    public void receiveDamage(int dmg) {
        if (dmg < 0)
            throw new IllegalArgumentException("Negative damage not allowed");

        if (checkDodge())
            return;

        int absorbed = Math.min(dmg, protection);
        int finalDmg = Math.max(0, dmg - absorbed);

        hp = Math.max(0, hp - finalDmg);
        protection -= absorbed;
    }

    /**
     * Checks if the hero can dodge an attack.
     * Consumes one level of DODGE status if available.
     * 
     * @return true if dodged, false otherwise.
     */
    private boolean checkDodge() {
        if (statusManager.getStatus(combat.Status.DODGE) > 0) {
            statusManager.removeStatus(combat.Status.DODGE, 1);
            return true;
        }
        return false;
    }

    /**
     * Gets the number of times the hero has refused a curse.
     * 
     * @return Curse refusal count.
     */
    public int getCurseRefusalCount() {
        return curseRefusalCount;
    }

    /**
     * Increments the curse refusal counter.
     */
    public void incrementCurseRefusal() {
        curseRefusalCount++;
    }

    /**
     * Resets the curse refusal counter to zero.
     */
    public void resetCurseRefusal() {
        curseRefusalCount = 0;
    }

    /**
     * Heals the hero by a given amount, without exceeding max HP.
     * 
     * @param amount The amount to heal.
     */
    public void heal(int amount) {
        if (amount < 0)
            return;
        hp += amount;
        if (hp > maxHp)
            hp = maxHp;
    }

    /**
     * Applies damage directly to HP, bypassing protection.
     * 
     * @param amount The amount of damage.
     */
    public void receiveDirectDamage(int amount) {
        if (amount < 0)
            return;
        hp -= amount;
        if (hp < 0)
            hp = 0;
    }

    /**
     * Increases protection by the given amount, applying HASTE and SLOW modifiers.
     * 
     * @param amount Base amount of protection to add.
     * @throws IllegalArgumentException if amount is negative.
     */
    public void increaseProtection(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative protection not allowed");
        }

        int haste = statusManager.getStatus(combat.Status.HASTE);
        int slow = statusManager.getStatus(combat.Status.SLOW);

        int finalAmt = amount + haste - slow;
        if (finalAmt < 0)
            finalAmt = 0;

        protection += finalAmt;
    }

    /**
     * Resets protection to zero.
     */
    public void resetProtection() {
        protection = 0;
    }

    /**
     * Adds experience points and handles level-ups.
     * Level-up grants unlock points for the backpack expansion.
     *
     * @param amount Amount of experience gained.
     * @throws IllegalArgumentException if amount is negative.
     */
    public void gainExp(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Negative experience gain not allowed");
        }
        exp += amount;

        while (exp >= expToLevel) {
            exp -= expToLevel;
            level++;
            // Award 3 to 4 expansion points
            int points = 3 + (int) (Math.random() * 2);
            backpack.addUnlockPoints(points);
            expToLevel += 10;
        }
    }

    /**
     * Checks if the hero is still alive.
     * 
     * @return true if HP > 0.
     */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * Deducts gold from the hero's backpack.
     * 
     * @param amount Amount to pay.
     */
    public void pay(int amount) {
        for (item.Item it : backpack.getItems()) {
            switch (it) {
                case item.Gold g -> {
                    int newAmt = g.amount() - amount;
                    backpack.remove(g);
                    if (newAmt > 0) {
                        addGold(newAmt);
                    }
                    return;
                }
                default -> {
                }
            }
        }
    }

    /**
     * Adds or removes gold from the hero.
     * 
     * @param amount Amount to add (negative value will trigger removal).
     */
    public void addGold(int amount) {
        if (amount < 0) {
            removeGold(-amount);
            return;
        }
        boolean[][] goldShape = { { true } };
        item.Item goldItem = new item.Gold(0, "Gold Pile", amount, true, item.Rarity.GOLD, new item.Shape(goldShape));
        backpack.add(goldItem);
    }

    /**
     * Internal method to remove a specific amount of gold from stacks.
     * 
     * @param amountToRemove Positive amount to remove.
     */
    private void removeGold(int amountToRemove) {
        if (amountToRemove <= 0)
            return;

        int remaining = amountToRemove;
        for (item.Gold gold : getGoldStacks()) {
            if (remaining <= 0)
                break;
            remaining = processGoldStack(gold, remaining);
        }
    }

    /**
     * Retrieves all gold items from the backpack.
     * 
     * @return List of Gold instances.
     */
    private java.util.List<item.Gold> getGoldStacks() {
        java.util.List<item.Gold> stacks = new java.util.ArrayList<>();
        for (item.Item it : backpack.getItems()) {
            switch (it) {
                case item.Gold g -> stacks.add(g);
                default -> {
                }
            }
        }
        return stacks;
    }

    /**
     * Processes a single gold stack for deduction.
     * 
     * @param gold      The gold item to process.
     * @param remaining The amount still needing to be removed.
     * @return The remaining amount to remove after processing this stack.
     */
    private int processGoldStack(item.Gold gold, int remaining) {
        Objects.requireNonNull(gold, "gold stack cannot be null");
        backpack.completelyRemoveItem(gold);
        if (gold.amount() > remaining) {
            addGold(gold.amount() - remaining);
            return 0;
        }
        return remaining - gold.amount();
    }

    /**
     * Returns hero's row position on the map.
     * 
     * @return Row index.
     */
    public int getRow() {
        return row;
    }

    /**
     * Sets hero's row position on the map.
     * 
     * @param row New row index.
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Returns hero's column position on the map.
     * 
     * @return Column index.
     */
    public int getCol() {
        return col;
    }

    /**
     * Sets hero's column position on the map.
     * 
     * @param col New column index.
     */
    public void setCol(int col) {
        this.col = col;
    }

    /**
     * Sets hero's position to given row and column.
     * 
     * @param row New row index.
     * @param col New column index.
     */
    public void setPos(int row, int col) {
        this.row = row;
        this.col = col;
    }
}
