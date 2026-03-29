package main;

import java.util.*;
import enemy.EnemyState;
import enemy.EnemyAction;
import enemy.EnemyList;
import hero.Hero;
import item.*;
import combat.Status;

/**
 * Manages the combat logic between the hero and a group of enemies.
 * Handles turns, actions, status effects, and curse mechanics.
 */
public class Combat {
    private Curse pendingCurse;
    private int refusedCurseCount = 0;
    private boolean waitingForCurseDecision = false;
    private boolean waitingForCursePlacement = false;

    private final Hero hero;
    private final List<EnemyState> enemies = new ArrayList<>();
    private final Random r = new Random();
    private List<List<EnemyAction>> currentEnemyPlans;

    // Loot tracking
    private int totalGoldLooted = 0;
    private final List<Item> itemsLooted = new ArrayList<>();

    /**
     * Constructs a new Combat instance.
     * Initializes the hero, spawns enemies, and prepares the first turn.
     * 
     * @param hero The hero participating in combat.
     */
    public Combat(Hero hero) {
        this.hero = Objects.requireNonNull(hero);
        spawnEnemies();
        initTurn();
    }

    /* ===== SETUP ===== */

    /**
     * Spawns a random number of enemies (1 to 3).
     */
    private void spawnEnemies() {
        int count = r.nextInt(3) + 1;
        for (int i = 0; i < count; i++) {
            enemies.add(EnemyList.createRandomEnemy());
        }
    }

    /**
     * Initializes a new turn for the hero.
     * Resets hero energy/protection and plans enemy actions.
     */
    private void initTurn() {
        hero.setEnergy(hero.getMaxEnergy());
        hero.resetProtection();

        startTurnStatusTicks(hero.getStatusManager(), hero);

        currentEnemyPlans = enemies.stream()
                .map(e -> e.isAlive()
                        ? e.getType().planAction()
                        : List.<EnemyAction>of())
                .toList();
    }

    /* ===== HERO ===== */

    /**
     * Executes an item usage action from the hero.
     * 
     * @param item       The item to use.
     * @param enemyIndex The index of the targeted enemy.
     * @return true if the action was successful, false otherwise.
     */
    public boolean heroUseItem(Item item, int enemyIndex) {
        Objects.requireNonNull(item, "item cannot be null");
        if (waitingForCurseDecision || waitingForCursePlacement)
            return false;
        return item.combatAction()
                .filter(a -> hero.getEnergy() >= a.energyCost())
                .map(a -> {
                    hero.setEnergy(hero.getEnergy() - a.energyCost());
                    return a.execute(this, enemyIndex);
                })
                .orElse(false);
    }

    /* ===== ENEMY ===== */

    /**
     * Returns a preview of the planned actions for all living enemies.
     * 
     * @return A list of action lists per enemy.
     */
    public List<List<EnemyAction>> previewEnemyActions() {
        return enemies.stream()
                .filter(EnemyState::isAlive)
                .map(e -> e.getType().planAction())
                .toList();
    }

    /**
     * Ends the hero's turn and triggers the enemy's turn logic.
     */
    public void endHeroTurn() {
        if (waitingForCurseDecision || waitingForCursePlacement)
            return;

        endTurnStatusTicks(hero.getStatusManager(), hero);
        if (!hero.isAlive())
            return;

        executeEnemyTurn();
        removeDeadEnemies();
        initTurn();
    }

    /**
     * Orchestrates the enemy turn: status ticks and action execution.
     */
    private void executeEnemyTurn() {
        enemies.forEach(EnemyState::resetProtection);
        var plans = getEnemyPlans();

        for (int i = 0; i < enemies.size(); i++) {
            EnemyState e = enemies.get(i);
            if (!e.isAlive())
                continue;

            startTurnStatusTicks(e.getStatusManager(), e);
            if (e.isAlive()) {
                executeActionsForEnemy(e, plans.get(i));
                endTurnStatusTicks(e.getStatusManager(), e);
            }
        }
    }

    /**
     * Executes all planned actions for a specific enemy.
     * 
     * @param e       The enemy state.
     * @param actions The list of actions to execute.
     */
    private void executeActionsForEnemy(EnemyState e, List<EnemyAction> actions) {
        Objects.requireNonNull(e, "enemy state cannot be null");
        Objects.requireNonNull(actions, "actions list cannot be null");
        for (EnemyAction action : actions) {
            if (!hero.isAlive())
                return;
            executeEnemyAction(e, action);
        }
    }

    /**
     * Executes a single enemy action.
     * 
     * @param e      The enemy state.
     * @param action The action to perform.
     */
    private void executeEnemyAction(EnemyState e, EnemyAction action) {
        Objects.requireNonNull(e, "enemy state cannot be null");
        Objects.requireNonNull(action, "action cannot be null");
        switch (action) {
            case EnemyAction.ATTACK -> performEnemyAttack(e);
            case EnemyAction.PROTECT -> e.increaseProtection();
            case EnemyAction.CURSE -> applyEnemyCurse();
            default -> {
            }
        }
    }

    /**
     * Calculates and applies damage from an enemy attack.
     * Handles RAGE/WEAK modifiers and hero SPIKES.
     * 
     * @param e The attacking enemy.
     */
    private void performEnemyAttack(EnemyState e) {
        Objects.requireNonNull(e, "enemy cannot be null");
        int rage = e.getStatusManager().getStatus(Status.RAGE);
        int weak = e.getStatusManager().getStatus(Status.WEAK);
        int finalDmg = Math.max(0, e.getAttackDamage() + rage - weak);
        hero.receiveDamage(finalDmg);

        int spikes = hero.getStatusManager().getStatus(Status.SPIKES);
        if (spikes > 0) {
            e.receiveDamage(spikes);
        }
    }

    /**
     * Process status effect ticks that occur at the start of a turn (Burn, Regen).
     * 
     * @param sm     The status manager to tick.
     * @param entity The entity (Hero or EnemyState) receiving the effect.
     */
    private void startTurnStatusTicks(combat.StatusManager sm, Object entity) {
        Objects.requireNonNull(sm, "status manager cannot be null");
        Objects.requireNonNull(entity, "entity cannot be null");
        // Burn
        int burn = sm.getStatus(Status.BURN);
        if (burn > 0) {
            applyDirectDamage(entity, burn);
            sm.removeStatus(Status.BURN, 1);
        }

        // Regeneration
        int regen = sm.getStatus(Status.REGENERATION);
        if (regen > 0) {
            heal(entity, regen);
            sm.removeStatus(Status.REGENERATION, 1);
        }
    }

    /**
     * Process status effect ticks that occur at the end of a turn (Poison, Curse
     * damage).
     * 
     * @param sm     The status manager to tick.
     * @param entity The entity receiving the effect.
     */
    private void endTurnStatusTicks(combat.StatusManager sm, Object entity) {
        Objects.requireNonNull(sm, "status manager cannot be null");
        Objects.requireNonNull(entity, "entity cannot be null");
        // Poison
        int poison = sm.getStatus(Status.POISON);
        if (poison > 0) {
            applyDirectDamage(entity, poison);
            sm.removeStatus(Status.POISON, 1);
        }

        // Passive Curse Damage (Hero only)
        switch (entity) {
            case Hero h -> {
                for (Item item : h.getBackPack().getItems()) {
                    switch (item) {
                        case Curse c -> h.receiveDamage(1);
                        default -> {
                        }
                    }
                }
            }
            default -> {
            }
        }
    }

    /**
     * Helper to apply direct damage to either Hero or EnemyState.
     */
    private void applyDirectDamage(Object entity, int amount) {
        Objects.requireNonNull(entity, "entity cannot be null");
        switch (entity) {
            case Hero h -> h.receiveDirectDamage(amount);
            case EnemyState e -> e.receiveDirectDamage(amount);
            default -> {
            }
        }
    }

    /**
     * Helper to apply healing to either Hero or EnemyState.
     */
    private void heal(Object entity, int amount) {
        Objects.requireNonNull(entity, "entity cannot be null");
        switch (entity) {
            case Hero h -> h.heal(amount);
            case EnemyState e -> e.heal(amount);
            default -> {
            }
        }
    }

    /**
     * Removes dead enemies from the list and processes their rewards.
     */
    public void removeDeadEnemies() {
        enemies.removeIf(e -> {
            if (!e.isAlive()) {
                processEnemyDeath(e);
                return true;
            }
            return false;
        });
    }

    /**
     * Grants exp and loot to the hero when an enemy dies.
     * 
     * @param e The dead enemy.
     */
    private void processEnemyDeath(EnemyState e) {
        Objects.requireNonNull(e, "enemy cannot be null");
        hero.gainExp(e.getExpReward());
        Item loot = item.ItemFactory.createEnemyLoot(e.getExpReward());

        switch (loot) {
            case Gold g -> {
                hero.addGold(g.amount());
                totalGoldLooted += g.amount();
                System.out.println("Loot: +" + g.amount() + " gold!");
            }
            default -> {
                hero.getBackPack().add(loot);
                itemsLooted.add(loot);
                System.out.println("Loot: " + loot.name() + " obtained!");
            }
        }
    }

    // ===== CURSE =====

    /**
     * Initiates the process of applying a curse to the hero.
     */
    private void applyEnemyCurse() {
        if (pendingCurse != null)
            return;

        int curseLevel = refusedCurseCount + 1;
        Shape shape = CurseShapes.random();

        pendingCurse = new Curse(
                0,
                "Curse",
                curseLevel,
                Rarity.CURSE,
                shape);

        waitingForCurseDecision = true;
    }

    /**
     * Returns the curse that is waiting to be accepted or placed.
     */
    public Curse getPendingCurse() {
        return pendingCurse;
    }

    /**
     * Ends the curse placement state.
     */
    public void finishCursePlacement() {
        pendingCurse = null;
        waitingForCursePlacement = false;
    }

    /**
     * Handles the hero's decision to refuse a curse, applying increasing damage.
     */
    public void refuseCurse() {
        if (!waitingForCurseDecision)
            return;

        refusedCurseCount++;
        int damage = refusedCurseCount;
        hero.receiveDamage(damage);

        pendingCurse = null;
        waitingForCurseDecision = false;
    }

    /**
     * Handles the hero's decision to accept a curse, enabling the placement view.
     */
    public void acceptCurse() {
        if (pendingCurse == null)
            return;

        waitingForCurseDecision = false;
        waitingForCursePlacement = true;
    }

    /**
     * Returns true if the hero is currently placing an accepted curse.
     */
    public boolean isWaitingForCursePlacement() {
        return waitingForCursePlacement;
    }

    /**
     * Returns true if the hero is deciding whether to accept a curse.
     */
    public boolean isWaitingForCurseDecision() {
        return waitingForCurseDecision;
    }

    /**
     * Attempts to place the pending curse in the backpack at the given coordinates.
     * 
     * @param backpack The hero's backpack.
     * @param gx       Grid X coordinate.
     * @param gy       Grid Y coordinate.
     * @return true if placement was successful.
     */
    public boolean placePendingCurse(BackPack backpack, int gx, int gy) {
        Objects.requireNonNull(backpack, "backpack cannot be null");
        if (!isWaitingForCursePlacement() || pendingCurse == null)
            return false;

        boolean success = backpack.placeCurse(pendingCurse, gx, gy);
        if (success)
            finishCursePlacement();
        return success;
    }

    /* ===== STATE ===== */

    /**
     * Checks if the combat session is over.
     * 
     * @return true if either hero or all enemies are dead.
     */
    public boolean isFinished() {
        return !hero.isAlive() || enemies.stream().noneMatch(EnemyState::isAlive);
    }

    /**
     * Returns the hero.
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Returns the list of enemies.
     */
    public List<EnemyState> getEnemies() {
        return enemies;
    }

    /**
     * Returns the current planned actions for all enemies.
     */
    public List<List<EnemyAction>> getEnemyPlans() {
        return currentEnemyPlans;
    }

    /**
     * Returns the total gold looted during this combat.
     */
    public int getTotalGoldLooted() {
        return totalGoldLooted;
    }

    /**
     * Returns the list of items looted during this combat.
     */
    public List<Item> getItemsLooted() {
        return new ArrayList<>(itemsLooted);
    }

    /**
     * Returns a summarized string of the loot obtained.
     */
    public String getLootSummary() {
        StringBuilder summary = new StringBuilder();

        if (totalGoldLooted > 0) {
            summary.append(totalGoldLooted).append(" gold");
        }

        if (!itemsLooted.isEmpty()) {
            if (summary.length() > 0) {
                summary.append(" | ");
            }
            summary.append("⚔️ ").append(itemsLooted.size()).append(" item(s)");
        }

        return summary.length() > 0 ? summary.toString() : "No loot";
    }

}
