package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import enemy.EnemyAction;
import enemy.EnemyState;
import hero.Hero;
import item.*;
import main.Combat;

public class CombatView implements GameView {

    private final Combat combat;
    private final GameView parentView;
    private final int floor;

    private EnemyMetrics enemyMetrics;
    private BackpackMetrics backpackMetrics;

    private int selectedEnemy = -1;
    private Item selectedItem = null;

    // For curse preview
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    // Hero rendering metrics
    private int heroImageX;
    private int heroImageY;
    private int heroImageSize;

    private boolean transitionToCursePlacement = false;

    /**
     * Constructs a CombatView.
     * 
     * @param combat     The combat model.
     * @param parentView The view to return to after combat.
     * @param floor      Current floor number.
     */
    public CombatView(Combat combat, GameView parentView, int floor) {
        this.combat = Objects.requireNonNull(combat);
        this.parentView = Objects.requireNonNull(parentView);
        this.floor = floor;
    }

    /* ===================== DRAW ===================== */

    @Override
    public void draw(Graphics2D g, int width, int height) {
        drawBackground(g, width, height);

        int contentW = width - (2 * PADDING);
        int contentH = height - (2 * PADDING);

        g.translate(PADDING, PADDING);
        renderGameElements(g, contentW, contentH);
        g.translate(-PADDING, -PADDING);
    }

    /**
     * Draws the combat screen background.
     */
    private void drawBackground(Graphics2D g, int width, int height) {
        GameView.drawFullScreenBackground(g, ImageManager.getImage("combat.png"), 0, 0, width, height,
                Color.BLACK);
    }

    /**
     * Renders all visual elements of the combat.
     */
    private void renderGameElements(Graphics2D g, int w, int h) {
        drawHero(g);
        drawEnemies(g, w, h);
        drawBackpack(g, w, h);
        drawHeroImage(g);
        drawUI(g, w, h);
    }

    /**
     * Draws the hero's status panel (health, energy, protection).
     */
    private void drawHero(Graphics2D g) {
        int x = 30, y = 20;
        drawHeroLabel(g, x, y);
        drawHealthBar(g, x, y);
        drawHeroStats(g, x, y);
    }

    /**
     * Draws the "HERO" label.
     */
    private void drawHeroLabel(Graphics2D g, int x, int y) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        g.setColor(Color.WHITE);
        g.drawString("HERO", x, y);
    }

    /**
     * Draws the hero's health bar.
     */
    private void drawHealthBar(Graphics2D g, int x, int y) {
        int barW = 200, barH = 15;
        Hero hero = combat.getHero();
        float percent = (float) hero.getHp() / hero.getMaxHp();

        // Using generalized progress bar (standardized squared style)
        GameView.drawProgressBar(g, x, y + 10, barW, barH, percent,
                new Color(50, 200, 50), Color.DARK_GRAY, false);

        drawHealthText(g, x, y, barW, barH, hero);
    }

    /**
     * Draws textual health information over the health bar.
     */
    private void drawHealthText(Graphics2D g, int x, int y, int w, int h, Hero hero) {
        g.setColor(Color.WHITE);
        g.drawRect(x, y + 10, w, h); // Square border to match the bar
        g.setFont(g.getFont().deriveFont(11f));
        String txt = hero.getHp() + " / " + hero.getMaxHp();
        GameView.drawCenteredText(g, txt, x + w / 2, y + 22);
    }

    /**
     * Draws numeric hero stats (Energy, Protection, Level).
     */
    private void drawHeroStats(Graphics2D g, int x, int y) {
        Hero h = combat.getHero();
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        g.drawString("Energy: " + h.getEnergy() + "/" + h.getMaxEnergy(), x + 220, y + 22);
        g.drawString("Protection: " + h.getProtection(), x + 340, y + 22);
        g.drawString("Level: " + h.getLevel(), x + 460, y + 22);
    }

    /**
     * Draws the visual image of the hero.
     */
    private void drawHeroImage(Graphics2D g) {
        if (backpackMetrics == null)
            return;

        calculateHeroImagePosition();
        drawShieldHighlight(g);

        var img = ImageManager.getImage("hero.png");
        if (img != null) {
            g.drawImage(img, heroImageX, heroImageY, heroImageSize, heroImageSize, null);
        }
    }

    /**
     * Calculates the rendering position for the hero image.
     */
    private void calculateHeroImagePosition() {
        heroImageSize = 200;
        heroImageX = backpackMetrics.startX() - heroImageSize - 30;
        heroImageY = backpackMetrics.startY()
                + (backpackMetrics.rows() * backpackMetrics.cellSize() - heroImageSize) / 2;
    }

    /**
     * Highlights the hero if a defensive item or consumable is selected.
     */
    private void drawShieldHighlight(Graphics2D g) {
        if (selectedItem == null)
            return;

        switch (selectedItem) {
            case item.Shield s -> highlightHero(g);
            case item.ManaShield ms -> highlightHero(g);
            case item.Consumable c -> highlightHero(g);
            default -> {
            }
        }

    }

    /**
     * Draws a highlight effect around the hero.
     */
    private void highlightHero(Graphics2D g) {
        g.setColor(new Color(100, 255, 100, 100)); // Transparent green
        g.fillRoundRect(heroImageX - 5, heroImageY - 5, heroImageSize + 10, heroImageSize + 10, 10, 10);

        g.setColor(Color.GREEN);
        g.setStroke(new java.awt.BasicStroke(3));
        g.drawRoundRect(heroImageX - 5, heroImageY - 5, heroImageSize + 10, heroImageSize + 10, 10, 10);

        g.setStroke(new java.awt.BasicStroke(1)); // Reset stroke
    }

    /**
     * Draws all enemies in the current combat.
     */
    private void drawEnemies(Graphics2D g, int contentWidth, int contentHeight) {
        List<EnemyState> enemies = combat.getEnemies();
        if (enemies.isEmpty()) {
            enemyMetrics = null;
            return;
        }

        enemyMetrics = calculateEnemyMetrics(contentWidth, contentHeight, enemies.size());
        drawEnemiesHeader(g);

        for (int i = 0; i < enemies.size(); i++) {
            drawSingleEnemy(g, enemies.get(i), i);
        }
    }

    /**
     * Draws the "ENEMIES" header.
     */
    private void drawEnemiesHeader(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("ENEMIES", enemyMetrics.startX(), enemyMetrics.startY() - 20);
    }

    /**
     * Draws a single enemy entry (sprite, selection, info).
     */
    private void drawSingleEnemy(Graphics2D g, EnemyState e, int index) {
        int y = enemyMetrics.startY() + index * enemyMetrics.lineSpacing();
        int x = enemyMetrics.startX();

        if (index == selectedEnemy)
            drawEnemySelection(g, x, y);
        drawEnemySprite(g, e, x, y);
        drawEnemyInfo(g, e, index, x, y);
    }

    /**
     * Draws a highlight effect for the selected enemy.
     */
    private void drawEnemySelection(Graphics2D g, int x, int y) {
        int w = enemyMetrics.enemyWidth() + 30;
        int h = enemyMetrics.enemyHeight() + 20;
        g.setColor(new Color(255, 255, 100, 40));
        g.fillRoundRect(x - 15, y - 10, w, h, 15, 15);
        g.setColor(new Color(255, 255, 100, 200));
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRoundRect(x - 15, y - 10, w, h, 15, 15);
        g.setStroke(new java.awt.BasicStroke(1));
    }

    /**
     * Draws the enemy's visual sprite.
     */
    private void drawEnemySprite(Graphics2D g, EnemyState e, int x, int y) {
        BufferedImage img = getEnemyImg(e);
        if (img == null)
            return;

        if (!e.isAlive())
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.3f));
        g.drawImage(img, x, y, enemyMetrics.enemyHeight(), enemyMetrics.enemyHeight(), null);
        if (!e.isAlive())
            g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Draws enemy name, stats, and intent (next actions).
     */
    private void drawEnemyInfo(Graphics2D g, EnemyState e, int index, int x, int y) {
        int infoX = x + enemyMetrics.enemyHeight() + 20;

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        g.drawString(e.getName().toUpperCase(), infoX, y + 25);

        drawEnemyStats(g, e, infoX, y);
        drawEnemyPlannedActions(g, e, combat.getEnemyPlans(), index, infoX + 180, y + 25);
    }

    /**
     * Draws enemy HP and Protection values.
     */
    private void drawEnemyStats(Graphics2D g, EnemyState e, int x, int y) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        g.setColor(new Color(255, 100, 100));
        g.drawString("HP: " + e.getHp() + " / " + e.getMaxHp(), x, y + 55);
        g.setColor(new Color(150, 150, 255));
        g.drawString("🛡️ PROTECTION: " + e.getProtection(), x, y + 80);
    }

    /**
     * Helper to load the enemy texture.
     */
    private BufferedImage getEnemyImg(EnemyState e) {
        // Direct call to method in EnemyType
        String path = e.getType().getTexturePath();
        return ImageManager.getImage(path);
    }

    /**
     * Renders the upcoming actions planned by an enemy.
     */
    private void drawEnemyPlannedActions(Graphics2D g, EnemyState enemy,
            List<List<EnemyAction>> enemyPlans, int index, int x, int y) {
        if (enemyPlans.isEmpty() || index >= enemyPlans.size())
            return;
        List<EnemyAction> actions = enemyPlans.get(index);
        int offsetY = 0;
        for (EnemyAction action : actions) {
            drawSinglePlannedAction(g, enemy, action, x, y + offsetY);
            offsetY += 20;
        }
    }

    /**
     * Draws a single intent icon/text for an enemy's next move.
     */
    private void drawSinglePlannedAction(Graphics2D g, EnemyState enemy, EnemyAction action, int x, int y) {
        String text;
        Color color;
        switch (action) {
            case ATTACK -> {
                text = "ATTACK (" + enemy.getAttackDamage() + ")";
                color = Color.RED;
            }
            case PROTECT -> {
                text = "PROTECT (+" + enemy.getProtectionGain() + ")";
                color = Color.CYAN;
            }
            default -> {
                text = getOtherActionText(action);
                color = getOtherActionColor(action);
            }
        }
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        g.setColor(color);
        g.drawString(text, x, y);
    }

    /**
     * Helper to get descriptive text for miscellaneous enemy actions.
     */
    private String getOtherActionText(EnemyAction action) {
        return action == EnemyAction.CURSE ? "CURSE" : action.name();
    }

    /**
     * Helper to get the display color for intent text.
     */
    private Color getOtherActionColor(EnemyAction action) {
        return action == EnemyAction.CURSE ? Color.MAGENTA : Color.WHITE;
    }

    /**
     * Calculates the rendering metrics for all enemies.
     */
    private EnemyMetrics calculateEnemyMetrics(int contentWidth, int contentHeight, int enemyCount) {
        int enemyWidth = Math.min(450, contentWidth / 2); // Wider for stats/actions
        int totalHeightAvailable = contentHeight - 120;
        int enemyHeight = Math.min(130, totalHeightAvailable / enemyCount - 40); // Image much larger
        int lineSpacing = enemyHeight + 60;
        int totalHeight = enemyCount * lineSpacing - 40;
        int startY = (contentHeight - totalHeight) / 2 + 30;
        int startX = contentWidth - enemyWidth - 60; // Realign to occupy right space
                                                     // enemies
        return new EnemyMetrics(startX, startY, enemyWidth, enemyHeight, lineSpacing);
    }

    private record EnemyMetrics(int startX, int startY, int enemyWidth, int enemyHeight, int lineSpacing) {
    }

    private record BackpackMetrics(int startX, int startY, int cellSize, int cols, int rows) {
    }

    /**
     * Calculates the layout metrics for the combat backpack grid.
     */
    private BackpackMetrics calculateBackpackMetrics(int screenW, int screenH, BackPack bp) {
        int cols = bp.getWidth();
        int rows = bp.getHeight();
        int heroZoneWidth = 250;
        int enemyZoneX = (int) (screenW * 0.6);
        int availableSpaceW = enemyZoneX - heroZoneWidth;
        int availableSpaceH = screenH - 100;

        int cellSize = calculateCellSize(availableSpaceW, availableSpaceH, cols, rows);
        int gridW = cols * cellSize;
        int gridH = rows * cellSize;

        int startX = heroZoneWidth + (availableSpaceW - gridW) / 2;
        int startY = (screenH - gridH) / 2;

        return new BackpackMetrics(startX, startY, cellSize, cols, rows);
    }

    /**
     * Calculates the ideal cell size for the backpack given available space.
     */
    private int calculateCellSize(int w, int h, int cols, int rows) {
        int cellW = w / (cols + 1);
        int cellH = h / (rows + 1);
        return Math.min(Math.min(cellW, cellH), 60);
    }

    /* ===================== BACKPACK ===================== */

    /**
     * Draws the backpack section during combat.
     */
    private void drawBackpack(Graphics2D g, int contentWidth, int contentHeight) {
        BackPack bp = combat.getHero().getBackPack();
        backpackMetrics = calculateBackpackMetrics(contentWidth, contentHeight, bp);

        drawBackpackGrid(g, bp);
        drawBackpackItems(g, bp);
        drawBackpackTitle(g);
    }

    /**
     * Draws the grid background cells for the backpack.
     */
    private void drawBackpackGrid(Graphics2D g, BackPack bp) {
        int startX = backpackMetrics.startX();
        int startY = backpackMetrics.startY();
        int cell = backpackMetrics.cellSize();

        for (int y = 0; y < backpackMetrics.rows(); y++) {
            for (int x = 0; x < backpackMetrics.cols(); x++) {
                if (bp.isActive(x, y)) {
                    int px = startX + x * cell;
                    int py = startY + y * cell;
                    drawCell(g, px, py, cell);
                }
            }
        }
    }

    /**
     * Draws a single grid cell background.
     */
    private void drawCell(Graphics2D g, int px, int py, int cell) {
        g.setColor(new Color(60, 60, 60, 150));
        g.fillRect(px, py, cell, cell);
        g.setColor(Color.GRAY);
        g.drawRect(px, py, cell, cell);
    }

    /**
     * Draws all items currently in the combat backpack.
     */
    private void drawBackpackItems(Graphics2D g, BackPack bp) {
        for (Item item : bp.getItems()) {
            drawItem(g, bp, item);
        }
    }

    /**
     * Draws the backpack title.
     */
    private void drawBackpackTitle(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("COMBAT BAG", backpackMetrics.startX(), backpackMetrics.startY() - 10);
    }

    /**
     * Draws a specific item within the combat backpack.
     */
    private void drawItem(Graphics2D g, BackPack bp, Item item) {
        if (backpackMetrics == null)
            return;

        // 1. Calculate precise screen position (pixels)
        Point screenPos = calculateItemScreenPosition(bp, item);

        // If item not found in grid, stop
        if (screenPos == null)
            return;

        // 2. Draw base texture
        drawItemTexture(g, item, screenPos);

        // 3. Draw specific info (Mana, etc.) via Switch
        drawItemOverlay(g, item, screenPos);

        // 4. Draw selection if necessary
        drawSelectionHighlight(g, item, screenPos);
    }

    /**
     * Calculates the X,Y coordinates in pixels of the item's top-left corner on
     * screen.
     * Returns null if the item is not in the backpack.
     */
    private Point calculateItemScreenPosition(BackPack bp, Item item) {
        Point gridPos = findItemGridPosition(bp, item);
        if (gridPos == null)
            return null;

        int cell = backpackMetrics.cellSize();
        int finalX = backpackMetrics.startX() + gridPos.x * cell;
        int finalY = backpackMetrics.startY() + gridPos.y * cell;

        return new Point(finalX, finalY);
    }

    /**
     * Finds the item's top-left position in the logical backpack grid.
     */
    private Point findItemGridPosition(BackPack bp, Item item) {
        return GameView.findItemGridPosition(bp, item);
    }

    /**
     * Displays the item's texture image.
     */
    private void drawItemTexture(Graphics2D g, Item item, Point pos) {
        String path = item.getTexturePath();
        BufferedImage img = ImageManager.getImage(path);
        int cell = backpackMetrics.cellSize();

        if (img != null) {
            g.drawImage(img, pos.x, pos.y,
                    item.shape().width() * cell,
                    item.shape().height() * cell, null);
        }
    }

    /**
     * Handles specific display depending on item type (e.g. Mana Text).
     */
    private void drawItemOverlay(Graphics2D g, Item item, Point pos) {
        switch (item) {
            case item.ManaStone stone -> drawManaText(g, stone, pos);
            default -> {
                /* Nothing to display for other items */ }
        }
    }

    /**
     * Displays mana text centered on the mana stone item.
     */
    private void drawManaText(Graphics2D g, item.ManaStone stone, Point pos) {
        int cell = backpackMetrics.cellSize();
        Shape shape = stone.shape();

        // Font configuration
        g.setColor(Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, (shape.height() * cell) / 3));

        String manaText = stone.currentMana() + "/" + stone.mana();

        // Centering calculation
        int textWidth = g.getFontMetrics().stringWidth(manaText);
        int textAscent = g.getFontMetrics().getAscent();

        int textX = pos.x + (shape.width() * cell) / 2 - textWidth / 2;
        int textY = pos.y + (shape.height() * cell) / 2 + textAscent / 2;

        g.drawString(manaText, textX, textY);
    }

    /**
     * Displays a yellow highlight if the item is selected for use.
     */
    private void drawSelectionHighlight(Graphics2D g, Item item, Point pos) {
        if (item != selectedItem)
            return;

        int cell = backpackMetrics.cellSize();
        Shape shape = item.shape();

        for (int dx = 0; dx < shape.width(); dx++) {
            for (int dy = 0; dy < shape.height(); dy++) {
                if (shape.get(dx, dy)) {
                    drawHighlightedCell(g, pos.x + dx * cell, pos.y + dy * cell, cell);
                }
            }
        }
    }

    private void drawHighlightedCell(Graphics2D g, int px, int py, int cell) {
        g.setColor(new Color(255, 255, 100, 80));
        g.fillRect(px + 1, py + 1, cell - 2, cell - 2);
        g.setColor(Color.YELLOW);
        g.drawRect(px, py, cell, cell);
    }

    /* ===================== UI ===================== */

    /**
     * Draws the main UI components (help, buttons, tooltips).
     */
    private void drawUI(Graphics2D g, int width, int height) {
        drawNavHelp(g, width, height);
        initializeButtons(width, height);
        drawContextualUI(g, width, height);

        if (selectedItem != null) {
            drawItemTooltip(g, selectedItem, width, height);
        }
        if (selectedEnemy >= 0) {
            drawTargetInfo(g, width);
        }
    }

    /**
     * Draws navigation and usage help text.
     */
    private void drawNavHelp(Graphics2D g, int width, int height) {
        g.setFont(g.getFont().deriveFont(12f));
        g.setColor(new Color(255, 255, 255, 180));
        g.drawString("Click item: select | Click enemy: target", 30, height - 20);
        g.drawString("E: end turn", width - 250, height - 20);
    }

    /**
     * Initializes UI button dimensions and positions.
     */
    private void initializeButtons(int width, int height) {
        int btnW = 280;
        int btnH = 60;
        int btnX = (width - btnW) / 2;
        int btnY = height - 60;

        endTurnButton = new UIButton(btnX, btnY, btnW, btnH, "END TURN");
        acceptCurseButton = new UIButton(btnX - 110, btnY, 200, btnH, "ACCEPT");
        refuseCurseButton = new UIButton(btnX + 110, btnY, 200, btnH, "REFUSE");
    }

    /**
     * Renders UI elements that vary based on the combat state (e.g., Curse
     * incoming).
     */
    private void drawContextualUI(Graphics2D g, int width, int height) {
        int btnY = height - 60;
        int btnW = 280;
        int btnX = (width - btnW) / 2;

        if (combat.getPendingCurse() == null) {
            drawEndTurnButton(g, btnX, btnY, btnW, 60);
        } else if (combat.isWaitingForCurseDecision()) {
            drawCurseButtons(g, width, btnY);
        }
    }

    private void drawEndTurnButton(Graphics2D g, int x, int y, int w, int h) {
        BufferedImage btnImg = ImageManager.getImage("endturn.png");
        if (btnImg != null) {
            g.drawImage(btnImg, x, y, w, h, null);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 20f));
            GameView.drawCenteredText(g, endTurnButton.label(), x + w / 2, y + 38);
        } else {
            drawButton(g, endTurnButton, new Color(100, 100, 100, 200));
        }
    }

    /**
     * Draws the confirm and refuse buttons for an incoming curse.
     */
    private void drawCurseButtons(Graphics2D g, int width, int btnY) {
        drawButton(g, acceptCurseButton, new Color(50, 150, 50, 200));
        drawButton(g, refuseCurseButton, new Color(150, 50, 50, 200));

        g.setColor(Color.MAGENTA);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        String msg = "A CURSE IS ARRIVING!";
        int mw = g.getFontMetrics().stringWidth(msg);
        g.drawString(msg, (width - mw) / 2, btnY - 20);
    }

    /**
     * Displays information about the currently targeted enemy.
     */
    private void drawTargetInfo(Graphics2D g, int width) {
        g.setColor(Color.YELLOW);
        g.drawString("Target: Enemy " + selectedEnemy, width - 150, 60);
    }

    private void drawItemTooltip(Graphics2D g, Item item, int width, int height) {
        int tw = 250, th = 100, tx = 30;
        int ty = height - 180;

        drawTooltipBox(g, tx, ty, tw, th);
        drawTooltipText(g, item, tx, ty);
        drawConsumableHint(g, item, tx, ty);
    }

    private void drawTooltipBox(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, w, h, 15, 15);
        g.setColor(new Color(255, 255, 100));
        g.setStroke(new java.awt.BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 15, 15);
    }

    /**
     * Draws lines of text describing item stats and cost.
     */
    private void drawTooltipText(Graphics2D g, Item item, int x, int y) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        g.drawString(item.name(), x + 15, y + 30);
        g.setFont(g.getFont().deriveFont(java.awt.Font.ITALIC, 13f));
        g.setColor(Color.WHITE);
        g.drawString(item.getStatDescription(), x + 15, y + 55);
        g.setFont(g.getFont().deriveFont(12f));
        g.setColor(new Color(200, 200, 255));
        g.drawString("Cost: " + item.energyCost() + " Energy", x + 15, y + 80);
    }

    /**
     * Draws a hint for using a selected item on the hero.
     */
    private void drawConsumableHint(Graphics2D g, Item item, int x, int y) {
        if (item instanceof Consumable || item instanceof Shield || item instanceof ManaShield) {
            g.setColor(Color.GREEN);
            g.drawString("Click the Hero to use", x + 15, y + 95);
        }
    }

    private record UIButton(int x, int y, int w, int h, String label) {
        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }

    private UIButton endTurnButton = new UIButton(30, 420, 180, 35, "Fin du tour");
    private UIButton acceptCurseButton = new UIButton(30, 420, 180, 35, "Accepter la malédiction");
    private UIButton refuseCurseButton = new UIButton(220, 420, 180, 35, "Refuser la malédiction");

    /**
     * Draws a standard UI button.
     */
    private void drawButton(Graphics2D g, UIButton b, Color bg) {
        GameView.drawPanel(g, b.x(), b.y(), b.w(), b.h(), bg, Color.WHITE, 10);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        GameView.drawCenteredText(g, b.label(), b.x() + b.w() / 2, b.y() + 25);
    }

    /* ===================== INPUT ===================== */

    @Override
    public void handlePointerEvent(PointerEvent event) {
        lastMouseX = (int) event.location().x();
        lastMouseY = (int) event.location().y();

        if (combat.isFinished() || event.action() != PointerEvent.Action.POINTER_DOWN)
            return;

        handleBackpackClick(lastMouseX, lastMouseY);
        handleEnemyClick(lastMouseX, lastMouseY);
        handleHeroClick(lastMouseX, lastMouseY);

        if (combat.getPendingCurse() == null) {
            handleEndTurnButton(lastMouseX, lastMouseY);
        } else {
            handleCurseButtons(lastMouseX, lastMouseY);
        }
    }

    /**
     * Handles clicks on the "End Turn" button.
     */
    private void handleEndTurnButton(int mx, int my) {
        if (endTurnButton.contains(mx, my)) {
            endTurn();
        }
    }

    /**
     * Handles clicks on the accept/refuse curse buttons.
     */
    private void handleCurseButtons(int mx, int my) {
        if (!combat.isWaitingForCurseDecision())
            return;
        if (acceptCurseButton.contains(mx, my)) {
            combat.acceptCurse();
            transitionToCursePlacement = true;
        } else if (refuseCurseButton.contains(mx, my)) {
            combat.refuseCurse();
        }
    }

    /**
     * Handles clicking on an active cell in the backpack.
     */
    private void handleBackpackClick(int mx, int my) {
        if (backpackMetrics == null)
            return;
        mx -= PADDING;
        my -= PADDING;

        Point gridPos = getGridPosition(mx, my);
        if (gridPos == null)
            return;

        int gx = gridPos.x;
        int gy = gridPos.y;

        BackPack bp = combat.getHero().getBackPack();
        if (!bp.isActive(gx, gy))
            return;

        selectItemAt(bp, gx, gy);
    }

    /**
     * Converts mouse coordinates to backpack grid coordinates.
     */
    private Point getGridPosition(int mx, int my) {
        int cell = backpackMetrics.cellSize();
        int startX = backpackMetrics.startX();
        int startY = backpackMetrics.startY();

        int gx = (mx - startX) / cell;
        int gy = (my - startY) / cell;

        if (gx < 0 || gy < 0 || gx >= backpackMetrics.cols() || gy >= backpackMetrics.rows()) {
            return null;
        }
        return new Point(gx, gy);
    }

    /**
     * Selects an item at a specific grid position.
     */
    private void selectItemAt(BackPack bp, int gx, int gy) {
        Item item = bp.getItemAt(gx, gy);
        if (item == null) {
            selectedItem = null;
            return;
        }

        if (item.combatAction().isEmpty())
            return;

        selectedItem = item;
        selectedEnemy = -1;
    }

    /**
     * Handles clicking on an enemy to target them.
     */
    private void handleEnemyClick(int mx, int my) {
        if (enemyMetrics == null)
            return;
        int index = findEnemyIndexAt(mx, my);
        if (index == -1)
            return;

        EnemyState enemy = combat.getEnemies().get(index);
        if (!enemy.isAlive())
            return;

        selectedEnemy = index;
        if (selectedItem != null) {
            selectedItem.combatAction().ifPresent(action -> {
                if (action.needsTarget()) {
                    combat.heroUseItem(selectedItem, selectedEnemy);
                }
            });
        }
    }

    /**
     * Identifies which enemy index is located at the given mouse coordinates.
     */
    private int findEnemyIndexAt(int mx, int my) {
        int x = enemyMetrics.startX(), y0 = enemyMetrics.startY();
        int w = enemyMetrics.enemyWidth(), h = enemyMetrics.enemyHeight();
        int spacing = enemyMetrics.lineSpacing();

        for (int i = 0; i < combat.getEnemies().size(); i++) {
            int y = y0 + i * spacing;
            if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handles clicks on the hero sprite (e.g., for using shields or potions).
     */
    private boolean handleHeroClick(int mx, int my) {
        boolean clickedOnHero = (mx >= heroImageX && mx <= heroImageX + heroImageSize &&
                my >= heroImageY && my <= heroImageY + heroImageSize);
        if (!clickedOnHero || selectedItem == null)
            return false;

        return useSelectedItemOnHero();
    }

    /**
     * Attempts to use the currently selected item on the hero.
     */
    private boolean useSelectedItemOnHero() {
        switch (selectedItem) {
            case item.Shield s -> combat.heroUseItem(s, -1);
            case item.ManaShield ms -> combat.heroUseItem(ms, -1);
            case item.Consumable c -> combat.heroUseItem(c, -1);
            default -> {
                return false;
            }
        }
        selectedItem = null;
        return true;
    }

    @Override
    public void handleKeyBoardEvent(KeyboardEvent event) {
        if (event.action() != KeyboardEvent.Action.KEY_PRESSED)
            return;

        switch (event.key()) {
            case KeyboardEvent.Key.E -> endTurn();
            case KeyboardEvent.Key.S -> {
                if (selectedItem != null) {
                    selectedItem.combatAction().ifPresent(action -> {
                        if (!action.needsTarget()) {
                            combat.heroUseItem(selectedItem, -1);
                            selectedItem = null;
                        }
                    });
                }
            }
            case KeyboardEvent.Key.A -> {
                if (combat.getPendingCurse() != null && combat.isWaitingForCurseDecision()) {
                    combat.acceptCurse();
                    transitionToCursePlacement = true; // <-- trigger the transition
                }
            }
            case KeyboardEvent.Key.R -> {
                if (combat.getPendingCurse() != null && combat.isWaitingForCurseDecision()) {
                    combat.refuseCurse();
                }
            }
            default -> {
            }
        }
    }

    /**
     * Resets combat state for the start of a new hero turn.
     */
    private void endTurn() {
        combat.endHeroTurn();
        selectedEnemy = -1;
        selectedItem = null;
    }

    /* ===================== LOOP ===================== */

    @Override
    public void updateLogic() {
        // no longer need to do anything here for the transition
    }

    @Override
    public boolean isDone() {
        return combat.isFinished() || transitionToCursePlacement;
    }

    @Override
    public GameView nextView() {
        // Ensure that dead enemies give XP before any transition
        combat.removeDeadEnemies();

        if (transitionToCursePlacement) {
            transitionToCursePlacement = false;
            return new CursePlacementView(combat, this);
        }

        Hero hero = combat.getHero();
        if (!hero.isAlive())
            return new GameOverView(hero, floor);

        if (combat.isWaitingForCursePlacement())
            return new CursePlacementView(combat, this);

        // Clear outside items when returning from combat
        hero.getBackPack().clearOutsideItems();

        return parentView;
    }

}
