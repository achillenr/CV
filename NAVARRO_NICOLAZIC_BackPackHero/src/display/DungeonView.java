package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Objects;

import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import hero.Hero;
import map.Dungeon;
import map.Floor;
import map.Room;
import map.RoomType;
import map.Treasure;
import item.BackPack;
import item.Item;
import item.ItemFactory;
import item.Key;
import main.Combat;
import item.Merchant;
import service.ScoreManager;
import map.Door;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.awt.image.BufferedImage;
// import display.Graphics;

public class DungeonView implements GameView {
    private final Dungeon dungeon;
    private final Hero hero;
    private int currentFloor = 0;
    private boolean returningFromCombat = false;

    // Rendering metrics for hit detection
    private int lastStartX;
    private int lastStartY;
    private int lastCellSize;
    private int lastWidth;
    private int lastHeight;

    // Prompt State
    private Room pendingInteractionRoom = null;
    private int promptMode = 0; // 0=None, 1=Unlock, 2=Exit
    private static final int BTN_W = 60;
    private static final int BTN_H = 30;

    // Message display
    private String displayMessage = null;
    private long messageStartTime = 0;
    private static final long MESSAGE_DURATION = 3000;

    // Pathfinding state
    private List<Point> currentPath = new ArrayList<>();
    private long lastMoveTime = 0;
    private static final long MOVE_DELAY = 150;

    private boolean done = false;
    private java.util.function.Supplier<GameView> nextView = () -> null;

    // Trophy rendering metrics
    private int lastTrophyX;
    private int lastTrophyY;
    private static final int TROPHY_SIZE = 50;

    // Combat loot tracking
    private Combat lastCombat = null;

    // Event State
    private event.DungeonEvent currentEvent = null;

    /**
     * Constructs a DungeonView.
     * 
     * @param dungeon The dungeon model.
     * @param hero    The hero instance.
     */
    public DungeonView(Dungeon dungeon, Hero hero) {
        this.dungeon = Objects.requireNonNull(dungeon, "dungeon cannot be null");
        this.hero = Objects.requireNonNull(hero, "hero cannot be null");
    }

    /**
     * Returns the current floor number (0-indexed).
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * Default constructor creating a new dungeon and hero.
     */
    public DungeonView() {
        this(new map.Dungeon(), new hero.Hero());
    }

    /**
     * Shows a temporary popup message on screen.
     */
    public void displayMessage(String msg) {
        this.displayMessage = msg;
        this.messageStartTime = System.currentTimeMillis();
    }

    /**
     * Clears the current event and message state.
     */
    public void finishEvent() {
        this.currentEvent = null;
        // Do not clear displayMessage here, so it persists in the HUD
    }

    /**
     * Triggers a combat sequence from an event result.
     */
    public void startCombatFromEvent() {
        finishEvent();
        Combat combat = new Combat(hero);
        lastCombat = combat;
        nextView = () -> {
            done = false;
            return new CombatView(combat, this, currentFloor);
        };
        done = true;
        returningFromCombat = true;
    }

    @Override
    public void draw(Graphics2D g, int width, int height) {

        // Render Event Overlay if active
        if (currentEvent != null) {
            drawEventUI(g, width, height);
        } else {
            // Only draw standard UI if no event
            drawUI(g, width, height);
        }
        // ...
    }

    /**
     * Renders the event decision UI over the map.
     */
    private void drawEventUI(Graphics2D g, int width, int height) {
        drawEventOverlay(g, width, height);

        int panelWidth = 600;
        int panelHeight = 400;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;

        drawEventPanel(g, panelX, panelY, panelWidth, panelHeight);
        drawEventChoices(g, panelX, panelY, panelWidth, panelHeight);
    }

    /**
     * Draws a semi-transparent black overlay for events.
     */
    private void drawEventOverlay(Graphics2D g, int width, int height) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, width, height);
    }

    /**
     * Draws the main text box for a dungeon event.
     */
    private void drawEventPanel(Graphics2D g, int x, int y, int w, int h) {
        GameView.drawPanel(g, x, y, w, h, new Color(50, 40, 40), new Color(200, 180, 100), 5);

        g.setColor(new Color(255, 215, 0));
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 24f));
        GameView.drawCenteredText(g, currentEvent.getTitle(), x + w / 2, y + 40);

        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN, 16f));
        String[] lines = currentEvent.getBody().split("\n");
        int textY = y + 80;
        for (String line : lines) {
            GameView.drawCenteredText(g, line, x + w / 2, textY);
            textY += 25;
        }

        if (displayMessage != null) {
            g.setColor(Color.CYAN);
            GameView.drawCenteredText(g, displayMessage, x + w / 2, textY + 20);
        }
    }

    /**
     * Draws the clickable choice buttons for a dungeon event.
     */
    private void drawEventChoices(Graphics2D g, int x, int y, int w, int h) {
        List<event.EventChoice> choices = currentEvent.getChoices();
        int btnWidth = 400;
        int btnHeight = 40;
        int startBtnY = y + h - (choices.size() * (btnHeight + 10)) - 20;

        for (int i = 0; i < choices.size(); i++) {
            event.EventChoice choice = choices.get(i);
            int btnX = x + (w - btnWidth) / 2;
            int btnY = startBtnY + i * (btnHeight + 10);

            GameView.drawPanel(g, btnX, btnY, btnWidth, btnHeight, new Color(80, 80, 100), Color.WHITE, 2);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
            GameView.drawCenteredText(g, choice.text(), btnX + btnWidth / 2, btnY + 25);
        }
    }

    // ... handlePointerEvent updates ...
    @Override
    public void handlePointerEvent(PointerEvent event) {
        if (event.action() != PointerEvent.Action.POINTER_DOWN)
            return;

        int mx = (int) event.location().x();
        int my = (int) event.location().y();

        if (currentEvent != null) {
            handleEventClick(mx, my);
            return;
        }

        handleStandardPointerEvent(event);
    }

    /**
     * Handles clicks when an event is active.
     */
    private void handleEventClick(int mx, int my) {
        int panelWidth = 600;
        int panelHeight = 400;
        int panelX = (lastWidth - panelWidth) / 2;
        int panelY = (lastHeight - panelHeight) / 2;

        List<event.EventChoice> choices = currentEvent.getChoices();
        int btnWidth = 400;
        int btnHeight = 40;
        int startBtnY = panelY + panelHeight - (choices.size() * (btnHeight + 10)) - 20;

        for (int i = 0; i < choices.size(); i++) {
            int btnX = panelX + (panelWidth - btnWidth) / 2;
            int btnY = startBtnY + i * (btnHeight + 10);

            if (mx >= btnX && mx <= btnX + btnWidth && my >= btnY && my <= btnY + btnHeight) {
                choices.get(i).action().accept(this);
                return;
            }
        }
    }

    private void drawTrophy(Graphics2D g, int width, int height) {
        // Direct call to ImageManager
        BufferedImage trophyImg = ImageManager.getImage("halloffametrophee.png");

        // If the image doesn't exist, display nothing (or a placeholder if desired)
        if (trophyImg == null)
            return;

        lastTrophyX = width - PADDING - TROPHY_SIZE - 10;
        lastTrophyY = height - PADDING - TROPHY_SIZE - 10;

        // Shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.fillOval(lastTrophyX + 5, lastTrophyY + 5, TROPHY_SIZE, TROPHY_SIZE);

        // Image
        g.drawImage(trophyImg, lastTrophyX, lastTrophyY, TROPHY_SIZE, TROPHY_SIZE, null);
    }

    /**
     * Returns the fallback color for a room type if the image is missing.
     */
    private Color colorForRoom(Room room) {
        if (room == null)
            return Color.DARK_GRAY;

        return switch (room.getRoomtype()) {
            case START -> new Color(80, 200, 80);
            case EXIT -> new Color(200, 80, 80);
            case ENEMY -> new Color(180, 60, 140);
            case MERCHANT -> new Color(200, 180, 60);
            case HEALER -> new Color(80, 160, 220);
            case EMPTY -> new Color(70, 70, 70);
            case CORRIDOR -> Color.GRAY;
            case DOOR -> Color.BLUE;
            case TREASURE -> Color.YELLOW;
            case EVENT -> new Color(100, 100, 255);
            default -> throw new IllegalArgumentException("Unexpected value: " + room.getRoomtype());
        };
    }

    /**
     * Draws the hero's statistics panel on the left side of the screen.
     */
    private void drawHeroStats(Graphics2D g, int width, int height) {
        int panelX = PADDING + 20;
        int panelWidth = 200;
        int panelHeight = 400;
        int panelY = (height - panelHeight) / 2;

        drawStatsBackground(g, panelX, panelY, panelWidth, panelHeight);

        int currentY = panelY + 20;
        int leftMargin = panelX + 15;

        currentY = drawStatsHeader(g, leftMargin, currentY, panelX, panelWidth);
        currentY = drawStatsLevelScore(g, leftMargin, currentY);
        currentY = drawStatsHealth(g, leftMargin, currentY, panelWidth);
        currentY = drawStatsGoldProtection(g, leftMargin, currentY, panelX, panelWidth);
        currentY = drawStatsExp(g, leftMargin, currentY, panelWidth, panelX);
        drawStatsFloor(g, leftMargin, currentY);
    }

    /**
     * Draws the background frame for the stats panel.
     */
    private void drawStatsBackground(Graphics2D g, int x, int y, int w, int h) {
        GameView.drawPanel(g, x, y, w, h, new Color(0, 0, 0, 180), new Color(200, 200, 200), 10);
    }

    /**
     * Draws the "HERO" header and separator line in stats panel.
     */
    private int drawStatsHeader(Graphics2D g, int leftMargin, int currentY, int panelX, int panelWidth) {
        int lineHeight = 25;
        g.setColor(new Color(255, 215, 0));
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        g.drawString("HERO", leftMargin, currentY);
        currentY += lineHeight + 5;

        g.setColor(new Color(150, 150, 150));
        g.drawLine(leftMargin, currentY, panelX + panelWidth - 15, currentY);
        return currentY + 15;
    }

    /**
     * Draws the hero's level and current calculated score.
     */
    private int drawStatsLevelScore(Graphics2D g, int leftMargin, int currentY) {
        int lineHeight = 25;
        g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN, 12f));

        g.setColor(new Color(255, 215, 0));
        g.drawString("Level: " + hero.getLevel(), leftMargin, currentY);
        currentY += lineHeight;

        g.setColor(Color.CYAN);
        g.drawString("Score: " + ScoreManager.calculateScore(hero, currentFloor), leftMargin, currentY);
        return currentY + lineHeight;
    }

    /**
     * Draws the hero's health bar and numeric HP value.
     */
    private int drawStatsHealth(Graphics2D g, int leftMargin, int currentY, int panelWidth) {
        int barWidth = panelWidth - 30;
        int barHeight = 15;
        int lineHeight = 25;

        g.setColor(Color.WHITE);
        g.drawString("HP: " + hero.getHp() + " / " + hero.getMaxHp(), leftMargin, currentY);
        currentY += 5;

        float hpPercent = (float) hero.getHp() / hero.getMaxHp();
        Color hpColor = (hpPercent > 0.5f) ? new Color(0, 200, 0)
                : (hpPercent > 0.25f) ? new Color(255, 165, 0) : new Color(200, 0, 0);

        GameView.drawProgressBar(g, leftMargin, currentY, barWidth, barHeight, hpPercent, hpColor,
                new Color(60, 60, 60), true);

        return currentY + barHeight + lineHeight;
    }

    /**
     * Draws the hero's gold and protection stats.
     */
    private int drawStatsGoldProtection(Graphics2D g, int leftMargin, int currentY, int panelX, int panelWidth) {
        int lineHeight = 25;
        g.setColor(new Color(255, 215, 0));
        g.drawString("Gold: " + hero.getGold(), leftMargin, currentY);
        currentY += lineHeight;

        if (hero.getProtection() > 0) {
            g.setColor(new Color(200, 200, 200));
            g.drawString("Protection: " + hero.getProtection(), leftMargin, currentY);
            currentY += lineHeight;
        }

        currentY += 5;
        g.setColor(new Color(150, 150, 150));
        g.drawLine(leftMargin, currentY, panelX + panelWidth - 15, currentY);
        return currentY + 15;
    }

    private int drawStatsExp(Graphics2D g, int leftMargin, int currentY, int panelWidth, int panelX) {
        int barWidth = panelWidth - 30;
        int barHeight = 15;
        int lineHeight = 25;

        g.setColor(Color.WHITE);
        g.drawString("EXP: " + hero.getExp() + " / " + hero.getExpToLevel(), leftMargin, currentY);
        currentY += 5;

        float expPercent = (float) hero.getExp() / hero.getExpToLevel();
        GameView.drawProgressBar(g, leftMargin, currentY, barWidth, barHeight, expPercent, new Color(100, 255, 100),
                new Color(60, 60, 60), true);

        currentY += barHeight + lineHeight;
        currentY += 5;
        g.setColor(new Color(150, 150, 150));
        g.drawLine(leftMargin, currentY, panelX + panelWidth - 15, currentY);
        return currentY + 15;
    }

    /**
     * Draws the current floor level.
     */
    private void drawStatsFloor(Graphics2D g, int leftMargin, int currentY) {
        g.setColor(new Color(255, 200, 100));
        g.drawString("Floor: " + (currentFloor + 1) + " / " + dungeon.getNumberOfFloors(), leftMargin, currentY);
    }

    /**
     * Draws the right side panel with action buttons and hints.
     */
    private void drawRightPanel(Graphics2D g, int width, int height) {
        int rightPanelWidth = 150;
        int panelX = width - PADDING - rightPanelWidth - 20;
        int panelHeight = 130;
        int panelY = (height - panelHeight) / 2;

        drawRightPanelBackground(g, panelX, panelY, rightPanelWidth, panelHeight);

        int currentY = panelY + 20;
        currentY = drawRightPanelHeader(g, panelX, rightPanelWidth, currentY);
        drawRightPanelContent(g, panelX, rightPanelWidth, currentY);
    }

    private void drawRightPanelBackground(Graphics2D g, int x, int y, int w, int h) {
        GameView.drawPanel(g, x, y, w, h, new Color(0, 0, 0, 180), new Color(200, 200, 200), 10);
    }

    private int drawRightPanelHeader(Graphics2D g, int panelX, int width, int currentY) {
        g.setColor(new Color(200, 200, 255));
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        String titleText = "ACTIONS";
        int titleCenterX = panelX + width / 2;
        GameView.drawCenteredText(g, titleText, titleCenterX, currentY);
        return currentY + 25;
    }

    /**
     * Fills the right panel with interactive hints and status text.
     */
    private void drawRightPanelContent(Graphics2D g, int panelX, int width, int currentY) {
        g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN, 11f));

        g.setColor(new Color(180, 180, 180));
        GameView.drawCenteredText(g, "Press B for Inventory", panelX + width / 2, currentY + 22);
        currentY += 35 + 10;

        if (hero.getBackPack().canExpand()) {
            g.setColor(Color.GREEN);
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 10f));
            GameView.drawCenteredText(g, "UPGRADE AVAILABLE!", panelX + width / 2, currentY);
            currentY += 15;
        }

        currentY += 15;
        g.setColor(new Color(180, 180, 180));
        g.setFont(g.getFont().deriveFont(java.awt.Font.PLAIN, 10f));
        GameView.drawCenteredText(g, "Click: move", panelX + width / 2, currentY);
    }

    /**
     * Draws the bottom message area for game notifications.
     */
    private void drawMessageArea(Graphics2D g, int width, int height) {
        int messageAreaHeight = 60;
        int messageAreaWidth = 600;
        int panelX = (width - messageAreaWidth) / 2;
        int panelY = height - PADDING - messageAreaHeight;

        drawMessagePanel(g, panelX, panelY, messageAreaWidth, messageAreaHeight);
        drawMessageContent(g, panelX, panelY, messageAreaWidth);
    }

    /**
     * Draws the background frame for the message area.
     */
    private void drawMessagePanel(Graphics2D g, int x, int y, int w, int h) {
        GameView.drawPanel(g, x, y, w, h, new Color(0, 0, 0, 200), new Color(200, 200, 200), 10);
    }

    /**
     * Draws the current text message or a default hint.
     */
    private void drawMessageContent(Graphics2D g, int panelX, int panelY, int width) {
        int currentY = panelY + 20;
        g.setColor(new Color(255, 255, 150));
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        GameView.drawCenteredText(g, "MESSAGES", panelX + width / 2, currentY);
        currentY += 20;

        if (displayMessage != null) {
            g.setColor(new Color(255, 200, 100));
            g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            GameView.drawCenteredText(g, displayMessage, panelX + width / 2, currentY);
        } else {
            g.setColor(new Color(150, 150, 150));
            g.setFont(g.getFont().deriveFont(java.awt.Font.ITALIC, 11f));
            GameView.drawCenteredText(g, "Click on a room to move...", panelX + width / 2, currentY);
        }
    }

    /**
     * Renders confirmation buttons (YES/NO) for interactive prompts.
     */
    private void drawPromptButtons(Graphics2D g, int width, int height) {
        if (promptMode == 0)
            return;
        int panelX = (width - 600) / 2;
        int panelY = height - PADDING - 60;

        int yesBtnX = panelX + 600 - 140;
        int noBtnX = panelX + 600 - 70;
        int btnY = panelY + (60 - 30) / 2;

        drawPromptButton(g, "YES", yesBtnX, btnY, new Color(40, 180, 40));
        drawPromptButton(g, "NO", noBtnX, btnY, new Color(180, 40, 40));
    }

    private void drawPromptButton(Graphics2D g, String text, int x, int y, Color color) {
        GameView.drawPanel(g, x, y, BTN_W, BTN_H, color, null, 5);
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        GameView.drawCenteredText(g, text, x + BTN_W / 2, y + 20);
    }

    @Override
    public void handleKeyBoardEvent(KeyboardEvent event) {
        if (event.action() != KeyboardEvent.Action.KEY_PRESSED)
            return;

        switch (event.key()) {
            case KeyboardEvent.Key.B -> openBackpack();
            case KeyboardEvent.Key.Q -> done = true;
            case KeyboardEvent.Key.UP, KeyboardEvent.Key.DOWN,
                    KeyboardEvent.Key.LEFT, KeyboardEvent.Key.RIGHT ->
                handleMovementKeys(event.key());
            default -> {
            }
        }
    }

    /**
     * Switches to the backpack view.
     */
    private void openBackpack() {
        nextView = () -> {
            done = false;
            return new BackPackView(hero.getBackPack(), this, hero);
        };
        done = true;
    }

    private void handleMovementKeys(KeyboardEvent.Key key) {
        switch (key) {
            case UP -> moveHero(-1, 0);
            case DOWN -> moveHero(1, 0);
            case LEFT -> moveHero(0, -1);
            case RIGHT -> moveHero(0, 1);
            default -> {
            }
        }
    }

    /**
     * Moves the hero by a delta in rows/cols if the move is valid and room exists.
     */
    private void moveHero(int dr, int dc) {
        if (hero.getRow() == -1)
            return; // Not initialized

        Floor floor = dungeon.getFloor(currentFloor);
        int nr = hero.getRow() + dr;
        int nc = hero.getCol() + dc;

        if (nr >= 0 && nr < floor.getRows() && nc >= 0 && nc < floor.getCols()) {
            Room target = floor.getRoom(nr, nc);
            if (target != null) {
                // Verify walkability if needed
                hero.setPos(nr, nc);
            }
        }
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public GameView nextView() {
        return nextView.get();
    }

    // @Override
    // public void handlePointerEvent(PointerEvent event) {
    // if (event.action() != PointerEvent.Action.POINTER_DOWN)
    // return;
    //
    // int mx = (int) event.location().x();
    // int my = (int) event.location().y();
    //
    // // Handle Prompt Clicks
    // if (promptMode != 0) {
    // int width = GameController.CONTENT_WIDTH;
    // // Check Yes
    // if (mx >= width - 150 && mx <= width - 150 + BTN_W && my >= BTN_Y && my <=
    // BTN_Y + BTN_H) {
    // if (promptMode == 1)
    // performUnlock();
    // else if (promptMode == 2)
    // performNextFloor();
    // promptMode = 0;
    // pendingInteractionRoom = null;
    // }
    // // Check No
    // else if (mx >= width - 80 && mx <= width - 80 + BTN_W && my >= BTN_Y && my <=
    // BTN_Y + BTN_H) {
    // promptMode = 0;
    // pendingInteractionRoom = null;
    // }
    // return; // Block other interaction
    // }
    //
    // if (lastCellSize == 0)
    // return;
    //
    // // Convert to grid coords
    // int c = (mx - lastStartX) / lastCellSize;
    // int r = (my - lastStartY) / lastCellSize;
    //
    // Floor floor = dungeon.getFloor(currentFloor);
    // // Check bounds
    // if (r >= 0 && r < floor.getRows() && c >= 0 && c < floor.getCols()) {
    // Room targetRoom = floor.getRoom(r, c);
    //
    // // Unlock Door Logic
    // if (targetRoom != null) {
    // if (targetRoom.getRoomtype() == RoomType.DOOR) {
    // if (targetRoom.getDoor() != null && !targetRoom.getDoor().isOpen()) {
    // // Check ownership of key
    // BackPack bp = hero.getBackPack();
    //
    // boolean hasKey = false;
    // for (Item it : bp.getItems()) {
    // switch (it) {
    // case Key k -> {
    // hasKey = true;
    // }
    // default -> {
    // }
    // }
    // if (hasKey)
    // break;
    // }
    //
    // if (hasKey) {
    // // Key found -> Prompt User
    // pendingInteractionRoom = targetRoom;
    // promptMode = 1; // Unlock Mode
    // return;
    // } else {
    // // No key -> Show message
    // displayMessage = "You don't have a key to open this door";
    // messageStartTime = System.currentTimeMillis();
    // return;
    // }
    // }
    // } else if (targetRoom.getRoomtype() == RoomType.EXIT) {
    // if (findPath(hero.getRow(), hero.getCol(), r, c) != null) {
    // pendingInteractionRoom = targetRoom;
    // promptMode = 2; // Exit Mode
    // return;
    // }
    // }
    // }
    //
    // List<Point> path = findPath(hero.getRow(), hero.getCol(), r, c);
    // if (path != null && !path.isEmpty()) {
    // currentPath = path;
    // lastMoveTime = System.currentTimeMillis();
    // }
    // }
    // }

    /**
     * Handles standard pointer events if no event is active.
     */
    private void handleStandardPointerEvent(PointerEvent event) {
        if (event.action() != PointerEvent.Action.POINTER_DOWN)
            return;

        int mx = (int) event.location().x();
        int my = (int) event.location().y();

        if (handleHallOfFameClick(mx, my))
            return;

        if (handlePromptClicks(mx, my))
            return;

        if (lastCellSize == 0)
            return;

        handleMapInteraction(mx, my);
    }

    /**
     * Handles clicking on the trophy icon to visit the Hall of Fame.
     */
    private boolean handleHallOfFameClick(int mx, int my) {
        if (mx >= lastTrophyX && mx <= lastTrophyX + TROPHY_SIZE && my >= lastTrophyY
                && my <= lastTrophyY + TROPHY_SIZE) {
            done = true;
            nextView = () -> new HallOfFameView(this);
            return true;
        }
        return false;
    }

    /**
     * Handles clicks on the YES/NO prompt buttons.
     */
    private boolean handlePromptClicks(int mx, int my) {
        if (promptMode == 0)
            return false;

        int messageAreaWidth = 600;
        int panelX = (lastWidth - messageAreaWidth) / 2;
        int panelY = lastHeight - PADDING - 60;
        int yesBtnX = panelX + messageAreaWidth - 140;
        int noBtnX = panelX + messageAreaWidth - 70;
        int btnY = panelY + (60 - 30) / 2;

        if (checkYesButton(mx, my, yesBtnX, btnY))
            return true;
        if (checkNoButton(mx, my, noBtnX, btnY))
            return true;

        return true; // Block other interaction
    }

    private boolean checkYesButton(int mx, int my, int yesBtnX, int btnY) {
        if (mx >= yesBtnX && mx <= yesBtnX + BTN_W && my >= btnY && my <= btnY + BTN_H) {
            if (promptMode == 1)
                performUnlock();
            else if (promptMode == 2)
                performNextFloor();
            resetPrompt();
            return true;
        }
        return false;
    }

    private boolean checkNoButton(int mx, int my, int noBtnX, int btnY) {
        if (mx >= noBtnX && mx <= noBtnX + BTN_W && my >= btnY && my <= btnY + BTN_H) {
            resetPrompt();
            return true;
        }
        return false;
    }

    /**
     * Resets the interactive prompt state.
     */
    private void resetPrompt() {
        promptMode = 0;
        displayMessage = null;
        pendingInteractionRoom = null;
    }

    /**
     * Main map interaction handler: translates screen click to room logic and
     * pathfinding.
     */
    private void handleMapInteraction(int mx, int my) {
        int c = (mx - lastStartX) / lastCellSize;
        int r = (my - lastStartY) / lastCellSize;

        Floor floor = dungeon.getFloor(currentFloor);
        if (r >= 0 && r < floor.getRows() && c >= 0 && c < floor.getCols()) {
            Room targetRoom = floor.getRoom(r, c);
            if (targetRoom != null) {
                processRoomClick(targetRoom, r, c);
            }
            updatePathTo(r, c);
        }
    }

    /**
     * Higher-level dispatcher for logic based on the clicked room type.
     */
    private void processRoomClick(Room targetRoom, int r, int c) {
        switch (targetRoom.getRoomtype()) {
            case DOOR -> handleDoorClick(targetRoom);
            case EXIT -> handleExitClick(targetRoom, r, c);
            case TREASURE -> handleTreasureClickChecked(targetRoom, r, c);
            default -> {
            }
        }
    }

    /**
     * Checks if a treasure can be accessed before handling the click.
     */
    private void handleTreasureClickChecked(Room targetRoom, int r, int c) {
        if (findPath(hero.getRow(), hero.getCol(), r, c) != null) {
            handleTreasureClick(targetRoom);
        } else {
            displayMessage = "Path blocked!";
            messageStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Triggers pathfinding to a target room.
     */
    private void updatePathTo(int r, int c) {
        List<Point> path = findPath(hero.getRow(), hero.getCol(), r, c);
        if (path != null && !path.isEmpty()) {
            currentPath = path;
            lastMoveTime = System.currentTimeMillis();
        }
    }

    /**
     * Handles interaction with doors (checking for keys and prompting).
     */
    private void handleDoorClick(Room targetRoom) {
        System.out.println("DOOR");
        if (targetRoom.getDoor() == null || targetRoom.getDoor().isOpen())
            return;

        if (checkForKey()) {
            pendingInteractionRoom = targetRoom;
            promptMode = 1;
            displayMessage = "Open this door?";
        } else {
            displayMessage = "You don't have a key to open this door";
            messageStartTime = System.currentTimeMillis();
        }
    }

    private boolean checkForKey() {
        for (Item it : hero.getBackPack().getItems()) {
            switch (it) {
                case Key k -> {
                    return true;
                }
                default -> {
                }
            }
        }
        return false;
    }

    /**
     * Handles clicking on the dungeon exit room.
     */
    private void handleExitClick(Room targetRoom, int r, int c) {
        System.out.println("EXIT");
        if (findPath(hero.getRow(), hero.getCol(), r, c) != null) {
            pendingInteractionRoom = targetRoom;
            promptMode = 2; // Exit
                            // Mode
            displayMessage = "Go to the next floor?";
        }
    }

    /**
     * Handles clicking on a treasure chest (giving loot).
     */
    private void handleTreasureClick(Room targetRoom) {
        System.out.println("TREASURE");
        Treasure t = targetRoom.getTreasure();
        if (t == null || t.isOpened()) {
            displayMessage = "Already empty.";
            messageStartTime = System.currentTimeMillis();
            return;
        }
        targetRoom.setTreasure(new Treasure(t.id(), true));
        // --- Loot Logic ---
        Item treasure = ItemFactory.createRandomTreasure();
        System.out.println("You found: " + treasure);
        BackPack bp = hero.getBackPack();
        bp.add(treasure);
        displayMessage = "You obtained: " + treasure.name();
        messageStartTime = System.currentTimeMillis();
    }

    @Override
    public void updateLogic() {
        // Clear message after duration (only if not in prompt mode)
        if (promptMode == 0 && displayMessage != null
                && System.currentTimeMillis() - messageStartTime > MESSAGE_DURATION) {
            displayMessage = null;
        }

        handleReturningFromCombat();
        handleMovementLogic();
    }

    /**
     * Shows a message with loot summary when returning from a successful combat.
     */
    private void handleReturningFromCombat() {
        if (!returningFromCombat)
            return;
        returningFromCombat = false;
        if (lastCombat != null) {
            displayMessage = "Combat over! Loot: " + lastCombat.getLootSummary();
            messageStartTime = System.currentTimeMillis();
            lastCombat = null;
        }
    }

    /**
     * Progresses the hero along the current movement path.
     */
    private void handleMovementLogic() {
        if (currentPath.isEmpty())
            return;
        long now = System.currentTimeMillis();
        if (now - lastMoveTime > MOVE_DELAY) {
            Point next = currentPath.remove(0);
            hero.setPos(next.y, next.x);
            lastMoveTime = now;
            if (currentPath.isEmpty()) {
                Floor floor = dungeon.getFloor(currentFloor);
                enterRoom(floor.getRoom(hero.getRow(), hero.getCol()));
            }
        }
    }

    /**
     * Executes logic when the hero enters a room.
     */
    private void enterRoom(Room room) {
        if (room == null)
            return;

        switch (room.getRoomtype()) {
            case ENEMY -> enterEnemyRoom(room);
            case TREASURE -> enterTreasureRoom(room);
            case HEALER -> enterHealerRoom(room);
            case MERCHANT -> enterMerchantRoom(room);
            case EVENT -> enterEventRoom(room);
            default -> {
            }
        }
    }

    /**
     * Initiates combat when entering an enemy room.
     */
    private void enterEnemyRoom(Room room) {
        hero.getBackPack().clearOutsideItems();
        hero.getBackPack().refillManaStones();
        Combat combat = new Combat(hero);
        lastCombat = combat;
        nextView = () -> {
            done = false;
            return new CombatView(combat, this, currentFloor);
        };
        done = true;
        room.setRoomType(RoomType.CORRIDOR);
        returningFromCombat = true;
    }

    private void enterTreasureRoom(Room room) {
        displayMessage = "You found a treasure!";
        messageStartTime = System.currentTimeMillis();
        room.setRoomType(RoomType.CORRIDOR);
    }

    /**
     * Heals the hero to full HP when entering a healer room.
     */
    private void enterHealerRoom(Room room) {
        hero.setMaxHp();
        displayMessage = "You have been healed";
        messageStartTime = System.currentTimeMillis();
        room.setRoomType(RoomType.CORRIDOR);
    }

    /**
     * Transitions to the MerchantView when entering a merchant room.
     */
    private void enterMerchantRoom(Room room) {
        Merchant merchant = new Merchant();
        nextView = () -> {
            done = false;
            return new MerchantView(merchant, hero, this);
        };
        room.setRoomType(RoomType.CORRIDOR);
        done = true;
    }

    private void enterEventRoom(Room room) {
        currentEvent = main.EventSystem.getRandomEvent(hero);
        room.setRoomType(RoomType.CORRIDOR);
    }

    /**
     * Uses BFS to find the shortest path between two points on the map.
     */
    private List<Point> findPath(int startR, int startC, int endR, int endC) {
        Floor floor = dungeon.getFloor(currentFloor);
        if (floor.getRoom(endR, endC) == null)
            return null;

        int rows = floor.getRows();
        int cols = floor.getCols();
        boolean[][] visited = new boolean[rows][cols];
        Point[][] parent = new Point[rows][cols];

        if (!runBFS(startR, startC, endR, endC, floor, visited, parent))
            return null;

        return reconstructPath(startC, startR, endC, endR, parent);
    }

    private boolean runBFS(int startR, int startC, int endR, int endC, Floor floor, boolean[][] visited,
            Point[][] parent) {
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startC, startR));
        visited[startR][startC] = true;

        while (!queue.isEmpty()) {
            Point curr = queue.poll();
            if (curr.y == endR && curr.x == endC)
                return true;

            exploreNeighbors(curr, floor, visited, parent, queue);
        }
        return false;
    }

    private void exploreNeighbors(Point curr, Floor floor, boolean[][] visited, Point[][] parent, Queue<Point> queue) {
        int[] dr = { -1, 1, 0, 0 };
        int[] dc = { 0, 0, -1, 1 };
        int rows = floor.getRows();
        int cols = floor.getCols();

        for (int i = 0; i < 4; i++) {
            int nr = curr.y + dr[i];
            int nc = curr.x + dc[i];

            if (isValidMove(nr, nc, rows, cols, floor, visited)) {
                visited[nr][nc] = true;
                parent[nr][nc] = new Point(curr.x, curr.y);
                queue.add(new Point(nc, nr));
            }
        }
    }

    private boolean isValidMove(int nr, int nc, int rows, int cols, Floor floor, boolean[][] visited) {
        if (nr < 0 || nr >= rows || nc < 0 || nc >= cols)
            return false;

        Room room = floor.getRoom(nr, nc);
        if (visited[nr][nc] || room == null || room.getRoomtype() == RoomType.EMPTY)
            return false;

        if (room.getRoomtype() == RoomType.DOOR) {
            return room.getDoor() == null || room.getDoor().isOpen();
        }
        return true;
    }

    private List<Point> reconstructPath(int startC, int startR, int endC, int endR, Point[][] parent) {
        List<Point> path = new ArrayList<>();
        Point curr = new Point(endC, endR);
        while (curr.x != startC || curr.y != startR) {
            path.add(0, curr);
            curr = parent[curr.y][curr.x];
        }
        return path;
    }

    /**
     * Consumes a key from the backpack to unlock the pending door.
     */
    private void performUnlock() {
        if (pendingInteractionRoom == null)
            return;
        Key keyToUse = findKeyInBackpack();
        if (keyToUse != null) {
            hero.getBackPack().completelyRemoveItem(keyToUse);
            pendingInteractionRoom.setDoor(new Door(pendingInteractionRoom.getDoor().id(), true));
            updatePathTo(pendingInteractionRoom.getRow(), pendingInteractionRoom.getCol());
        }
    }

    private Key findKeyInBackpack() {
        for (Item it : hero.getBackPack().getItems()) {
            switch (it) {
                case Key k -> {
                    return k;
                }
                default -> {
                }
            }
        }
        return null;
    }

    /**
     * Transitions the game state to the next dungeon floor.
     */
    private void performNextFloor() {
        List<Point> path = findPath(hero.getRow(), hero.getCol(), pendingInteractionRoom.getRow(),
                pendingInteractionRoom.getCol());
        if (path != null && !path.isEmpty()) {
            currentPath = path;
            lastMoveTime = System.currentTimeMillis();
        }
        currentFloor++;
        if (currentFloor >= dungeon.getNumberOfFloors()) {
            dungeon.addFloor(Floor.createDefaultFloor());
        }
        hero.setPos(-1, -1);
        currentPath.clear();
    }

    @Override
    public void reset() {
        this.done = false;
        this.nextView = () -> null;
    }

    public void resetAfterCombat() {
        reset();
    }

    private void drawUI(Graphics2D g, int width, int height) {
        ensureFloorExists();
        lastWidth = width;
        lastHeight = height;

        drawGlobalBackground(g, width, height);

        Floor floor = dungeon.getFloor(currentFloor);
        int rows = floor.getRows();
        int cols = floor.getCols();

        calculateAndStoreMetrics(width, height, cols, rows);

        drawMapGrid(g, floor, rows, cols);
        drawHeroOnMap(g);
        drawOverlayUI(g, width, height);
    }

    /**
     * Ensures that at least one floor exists in the dungeon.
     */
    private void ensureFloorExists() {
        if (dungeon.getNumberOfFloors() == 0) {
            dungeon.addFloor(Floor.createDefaultFloor());
        }
    }

    /**
     * Draws the main background image for the dungeon view.
     */
    private void drawGlobalBackground(Graphics2D g, int width, int height) {
        BufferedImage bg = ImageManager.getImage("background.png");
        GameView.drawFullScreenBackground(g, bg, 0, 0, width, height, new Color(30, 20, 20));
    }

    /**
     * Calculates the rendering positions and cell size based on screen dimensions.
     */
    private void calculateAndStoreMetrics(int width, int height, int cols, int rows) {
        int contentWidth = width - (2 * PADDING);
        int contentHeight = height - (2 * PADDING);
        int statsPanelWidth = 200;
        int rightPanelWidth = 150;
        int messageAreaHeight = 60;

        int mapAreaWidth = contentWidth - statsPanelWidth - rightPanelWidth - (2 * PADDING);
        int mapAreaHeight = contentHeight - messageAreaHeight - PADDING;

        int calculatedCellSize = Math.min(mapAreaWidth / cols, mapAreaHeight / rows);
        lastCellSize = (int) (calculatedCellSize * 0.75);

        int mapWidth = cols * lastCellSize;
        int mapHeight = rows * lastCellSize;

        int mapAreaStartX = PADDING + statsPanelWidth + PADDING;
        lastStartX = mapAreaStartX + (mapAreaWidth - mapWidth) / 2;
        lastStartY = PADDING + (mapAreaHeight - mapHeight) / 2;
    }

    private void drawMapGrid(Graphics2D g, Floor floor, int rows, int cols) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = lastStartX + c * lastCellSize;
                int y = lastStartY + r * lastCellSize;
                drawRoom(g, floor.getRoom(r, c), x, y, r, c);
            }
        }
    }

    /**
     * Renders a single room on the map grid.
     */
    private void drawRoom(Graphics2D g, Room room, int x, int y, int r, int c) {
        String path = room.getRoomtype().getImagePath();
        BufferedImage roomImg = (path != null) ? ImageManager.getImage(path) : null;

        if (roomImg != null) {
            g.drawImage(roomImg, x, y, lastCellSize, lastCellSize, null);
        } else {
            g.setColor(colorForRoom(room));
            g.fillRect(x, y, lastCellSize, lastCellSize);
        }

        g.setColor(new Color(0, 0, 0, 50));
        g.drawRect(x, y, lastCellSize, lastCellSize);

        if (room.getRoomtype() == RoomType.DOOR && room.getDoor() != null) {
            drawDoorStatus(g, room, x, y);
        }
        checkStartPos(room, r, c);
    }

    /**
     * Renders the "OPEN" or "CLOSED" label on doors.
     */
    private void drawDoorStatus(Graphics2D g, Room room, int x, int y) {
        g.setColor(Color.WHITE);
        java.awt.Font original = g.getFont();
        g.setFont(original.deriveFont(java.awt.Font.BOLD, 12f));
        String s = room.getDoor().isOpen() ? "OPEN" : "CLOSED";
        int sw = g.getFontMetrics().stringWidth(s);
        g.drawString(s, x + (lastCellSize - sw) / 2, y + lastCellSize / 2 + 5);
        g.setFont(original);
    }

    private void checkStartPos(Room room, int r, int c) {
        if (hero.getRow() == -1 || hero.getCol() == -1) {
            if (room != null && room.getRoomtype() == RoomType.START) {
                hero.setPos(r, c);
            }
        }
    }

    /**
     * Renders the hero sprite on the map, including movement interpolation.
     */
    private void drawHeroOnMap(Graphics2D g) {
        if (hero.getRow() == -1)
            return;

        Point.Float drawPos = calculateHeroDrawPosition();
        int hx = (int) (lastStartX + drawPos.x * lastCellSize);
        int hy = (int) (lastStartY + drawPos.y * lastCellSize);

        BufferedImage heroImg = ImageManager.getImage("hero.png");
        if (heroImg != null) {
            g.drawImage(heroImg, hx, hy, lastCellSize, lastCellSize, null);
        } else {
            g.setColor(Color.BLUE);
            g.fillOval(hx, hy, lastCellSize, lastCellSize);
        }
    }

    /**
     * Calculates the animated position of the hero during movement.
     */
    private Point.Float calculateHeroDrawPosition() {
        if (currentPath.isEmpty()) {
            return new Point.Float(hero.getCol(), hero.getRow());
        }
        long now = System.currentTimeMillis();
        float progress = (float) (now - lastMoveTime) / MOVE_DELAY;
        progress = Math.min(Math.max(progress, 0), 1);
        Point next = currentPath.get(0);
        float dc = hero.getCol() + (next.x - hero.getCol()) * progress;
        float dr = hero.getRow() + (next.y - hero.getRow()) * progress;
        return new Point.Float(dc, dr);
    }

    private void drawOverlayUI(Graphics2D g, int width, int height) {
        drawHeroStats(g, width, height);
        drawRightPanel(g, width, height);
        drawMessageArea(g, width, height);
        drawPromptButtons(g, width, height);
        drawTrophy(g, width, height);
    }
}
