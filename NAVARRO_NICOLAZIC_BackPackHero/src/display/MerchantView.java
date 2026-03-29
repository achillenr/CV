package display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import hero.Hero;
import item.BackPack;
import item.Item;
import item.Merchant;
import item.Rarity;
import item.Shape;

public class MerchantView implements GameView {

	private final Merchant merchant;
	private final Hero hero;
	private final GameView parentView;
	private boolean done = false;

	private int selectedShopItem = -1;
	private Item selectedBackpackItem = null;

	private String fadingMessage = "";
	private int fadingTimer = 0;
	private java.awt.Rectangle buyButtonRect;

	private Metrics lastMetrics;

	/**
	 * Constructs a MerchantView.
	 * 
	 * @param merchant   The merchant instance.
	 * @param hero       The hero instance.
	 * @param parentView The view to return to.
	 */
	public MerchantView(Merchant merchant, Hero hero, GameView parentView) {
		this.merchant = Objects.requireNonNull(merchant);
		this.hero = Objects.requireNonNull(hero);
		this.parentView = Objects.requireNonNull(parentView);
	}

	private record Metrics(int startX_BP, int startY_BP, int cellSize_BP,
			int startX_Shop, int startY_Shop, int cellSize_Shop,
			int shopItemW, int shopItemH) {
	}

	/**
	 * Calculates the visual layout metrics for the merchant UI.
	 */
	private Metrics calculateMetrics(int width, int height) {
		Point bpStart = calculateBackpackStart(width, height);
		int bpSize = calculateBackpackCellSize(width, height);
		return new Metrics(bpStart.x, bpStart.y, bpSize, width / 2 + 50, 100, 40, width / 2 - 100, 80);
	}

	private int calculateBackpackCellSize(int width, int height) {
		BackPack bp = hero.getBackPack();
		int size = Math.min(width / (bp.getWidth() + 10), height / (bp.getHeight() + 10));
		return Math.max(30, size);
	}

	private Point calculateBackpackStart(int width, int height) {
		BackPack bp = hero.getBackPack();
		int size = calculateBackpackCellSize(width, height);
		return new Point(50, (height - (bp.getHeight() * size)) / 2);
	}

	@Override
	public void draw(Graphics2D g, int width, int height) {
		BufferedImage bg = ImageManager.getImage("shop.png");
		GameView.drawFullScreenBackground(g, bg, 0, 0, width, height, new Color(40, 30, 20));

		this.lastMetrics = calculateMetrics(width, height);

		drawBackpack(g);
		drawHeroStats(g);
		drawShop(g);
		drawUI(g, width, height);
		drawFadingMessage(g, width, height);
	}

	/**
	 * Renders the hero's current gold amount.
	 */
	private void drawHeroStats(Graphics2D g) {
		Metrics m = lastMetrics;
		g.setColor(Color.YELLOW);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
		g.drawString("YOUR GOLD: " + hero.getGold(), m.startX_BP, m.startY_BP - 40);
	}

	/**
	 * Draws the hero's backpack on the left.
	 */
	private void drawBackpack(Graphics2D g) {
		Metrics m = lastMetrics;
		g.setColor(Color.WHITE);
		g.drawString("YOUR BAG", m.startX_BP, m.startY_BP - 10);

		drawBackpackGrid(g, m);
		drawBackpackItems(g, m);
	}

	/**
	 * Draws the grid background for the backpack.
	 */
	private void drawBackpackGrid(Graphics2D g, Metrics m) {
		BackPack bp = hero.getBackPack();
		for (int r = 0; r < bp.getHeight(); r++) {
			for (int c = 0; c < bp.getWidth(); c++) {
				if (bp.isActive(c, r)) {
					int px = m.startX_BP + c * m.cellSize_BP;
					int py = m.startY_BP + r * m.cellSize_BP;
					g.setColor(new Color(60, 60, 60, 150));
					g.fillRect(px, py, m.cellSize_BP, m.cellSize_BP);
					g.setColor(Color.GRAY);
					g.drawRect(px, py, m.cellSize_BP, m.cellSize_BP);
				}
			}
		}
	}

	/**
	 * Draws the items contained in the hero's backpack.
	 */
	private void drawBackpackItems(Graphics2D g, Metrics m) {
		BackPack bp = hero.getBackPack();
		for (Item item : bp.getItems()) {
			Point p = findItemGridPosition(bp, item);
			if (p != null) {
				drawItem(g, item, m.startX_BP + p.x * m.cellSize_BP, m.startY_BP + p.y * m.cellSize_BP, m.cellSize_BP,
						item == selectedBackpackItem);
			}
		}
	}

	/**
	 * Draws the merchant's shop list on the right.
	 */
	private void drawShop(Graphics2D g) {
		Metrics m = lastMetrics;
		g.setColor(Color.WHITE);
		g.drawString("MERCHANT SHOP", m.startX_Shop, m.startY_Shop - 15);

		List<Item> shopItems = merchant.getShop();
		for (int i = 0; i < shopItems.size(); i++) {
			drawShopItem(g, i, shopItems.get(i), m);
		}
	}

	private void drawShopItem(Graphics2D g, int i, Item item, Metrics m) {
		int y = m.startY_Shop + i * (m.shopItemH + 10);
		if (i == selectedShopItem) {
			g.setColor(new Color(255, 255, 0, 50));
			g.fillRect(m.startX_Shop - 5, y - 5, m.shopItemW + 10, m.shopItemH + 10);
		}

		GameView.drawPanel(g, m.startX_Shop, y, m.shopItemW, m.shopItemH,
				new Color(40, 40, 40, 200), Color.LIGHT_GRAY, 5);

		drawItem(g, item, m.startX_Shop + 10, y + (m.shopItemH - 40) / 2, 20, false);

		g.setColor(Color.WHITE);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
		g.drawString(item.name(), m.startX_Shop + 60, y + 25);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
		g.drawString(item.getStatDescription(), m.startX_Shop + 60, y + 45);
		g.setColor(Color.YELLOW);
		g.drawString(merchant.getPrice(i) + " G", m.startX_Shop + 60, y + 65);
	}

	/**
	 * Draws an item including its shape and texture.
	 */
	private void drawItem(Graphics2D g, Item item, int x, int y, int size, boolean selected) {
		drawItemShape(g, item, x, y, size, selected);
		drawItemTexture(g, item, x, y, size);
	}

	/**
	 * Draws the colored grid shape of an item.
	 */
	private void drawItemShape(Graphics2D g, Item item, int x, int y, int size, boolean selected) {
		Shape shape = item.shape();
		Color base = GameView.getColorByRarity(item.rarity());
		Color fill = selected ? new Color(255, 255, 100, 150)
				: new Color(base.getRed(), base.getGreen(), base.getBlue(), 100);

		Point min = findMinXY(shape);
		for (int i = 0; i < shape.width(); i++) {
			for (int j = 0; j < shape.height(); j++) {
				if (shape.get(i, j)) {
					int px = x + (i - min.x) * size;
					int py = y + (j - min.y) * size;
					g.setColor(fill);
					g.fillRect(px + 1, py + 1, size - 2, size - 2);
					g.setColor(selected ? Color.YELLOW : Color.BLACK);
					g.drawRect(px, py, size, size);
				}
			}
		}
	}

	/**
	 * Finds the minimum X and Y coordinates with content in a shape's matrix.
	 */
	private Point findMinXY(Shape s) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		for (int i = 0; i < s.width(); i++) {
			for (int j = 0; j < s.height(); j++) {
				if (s.get(i, j)) {
					minX = Math.min(minX, i);
					minY = Math.min(minY, j);
				}
			}
		}
		return new Point(minX, minY);
	}

	/**
	 * Draws the item's texture image.
	 */
	private void drawItemTexture(Graphics2D g, Item item, int x, int y, int size) {
		BufferedImage img = ImageManager.getImage(item.getTexturePath());
		if (img != null) {
			g.drawImage(img, x, y, item.shape().width() * size, item.shape().height() * size, null);
		} else {
			g.setColor(Color.RED);
			g.drawString("?", x + size / 2, y + size / 2);
		}
	}

	/**
	 * Draws the general UI (navigation hints, selling options).
	 */
	private void drawUI(Graphics2D g, int w, int h) {
		g.setColor(Color.WHITE);
		g.drawString("SPACE: Exit", 50, h - 50);

		if (selectedBackpackItem != null && selectedBackpackItem.isSellable()) {
			g.drawString("S: Sell selected item", 500, h - 50);
		}
		drawItemInfobox(g, w, h);
	}

	/**
	 * Draws a message that fades out over time (e.g., "Not enough gold").
	 */
	private void drawFadingMessage(Graphics2D g, int w, int h) {
		if (fadingTimer > 0) {
			Metrics m = lastMetrics;
			int alpha = Math.min(255, fadingTimer * 5);
			g.setColor(new Color(255, 255, 0, alpha));
			g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
			int goldW = g.getFontMetrics().stringWidth("YOUR GOLD: " + hero.getGold());
			int centerX = m.startX_BP + goldW / 2;
			GameView.drawCenteredText(g, fadingMessage, centerX, m.startY_BP - 70);
			fadingTimer--;
		}
	}

	/**
	 * Draws the details box for the currently selected item.
	 */
	private void drawItemInfobox(Graphics2D g, int w, int h) {
		Item selected = getSelectedItem();
		if (selected == null) {
			buyButtonRect = null;
			return;
		}

		Metrics m = lastMetrics;
		int boxW = 300, boxH = 220, x = m.startX_BP;
		int y = m.startY_BP + (hero.getBackPack().getHeight() * m.cellSize_BP) + 20;

		drawInfoPanel(g, x, y, boxW, boxH, selected);
		drawShapePreview(g, x, y, boxW, selected.shape());
		drawInfoText(g, x, y, boxH, selected);
		drawBuyButtonIfNeeded(g, x, y, boxW, boxH);
	}

	/**
	 * Returns the currently focused item (either from shop or backpack).
	 */
	private Item getSelectedItem() {
		if (selectedShopItem != -1)
			return merchant.getShop().get(selectedShopItem);
		return selectedBackpackItem;
	}

	/**
	 * Draws the background and header for the info panel.
	 */
	private void drawInfoPanel(Graphics2D g, int x, int y, int w, int h, Item selected) {
		GameView.drawPanel(g, x, y, w, h, new Color(0, 0, 0, 200), Color.WHITE, 10);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
		g.setColor(Color.WHITE);
		g.drawString(selected.name(), x + 10, y + 25);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 11));
		g.drawString(selected.rarity().toString(), x + 10, y + 40);
	}

	/**
	 * Draws a mini preview of the item's shape in the info box.
	 */
	private void drawShapePreview(Graphics2D g, int x, int y, int boxW, Shape s) {
		int cellSize = 15;
		int shapeX = x + (boxW - s.width() * cellSize) / 2;
		int shapeY = y + 60;
		for (int i = 0; i < s.width(); i++) {
			for (int j = 0; j < s.height(); j++) {
				if (s.get(i, j)) {
					g.setColor(new Color(255, 255, 255, 40));
					g.fillRect(shapeX + i * cellSize, shapeY + j * cellSize, cellSize, cellSize);
					g.setColor(new Color(255, 255, 255, 80));
					g.drawRect(shapeX + i * cellSize, shapeY + j * cellSize, cellSize, cellSize);
				}
			}
		}
	}

	/**
	 * Draws the descriptive stats and price information for an item.
	 */
	private void drawInfoText(Graphics2D g, int x, int y, int boxH, Item s) {
		g.setColor(Color.WHITE);
		g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
		int ty = y + boxH - 80;
		g.drawString(s.getStatDescription(), x + 10, ty);
		if (s.energyCost() > 0)
			g.drawString("Energy cost: " + s.energyCost(), x + 10, ty += 15);
		if (s.manaCost() > 0)
			g.drawString("Mana cost: " + s.manaCost(), x + 10, ty += 15);
		if (s.isSellable()) {
			g.setColor(Color.YELLOW);
			g.drawString("Sell price: " + merchant.calculateBasePrice(s) + " G", x + 10, ty + 15);
		}
	}

	/**
	 * Draws the "Buy" button if a shop item is selected.
	 */
	private void drawBuyButtonIfNeeded(Graphics2D g, int x, int y, int boxW, int boxH) {
		if (selectedShopItem == -1) {
			buyButtonRect = null;
			return;
		}
		int bw = 100, bh = 30;
		int bx = x + (boxW - bw) / 2, by = y + boxH - 40;
		buyButtonRect = new java.awt.Rectangle(bx, by, bw, bh);
		GameView.drawPanel(g, bx, by, bw, bh, Color.GREEN.darker(), Color.WHITE, 5);
		g.setColor(Color.WHITE);
		g.drawString("BUY", bx + 25, by + 20);
	}

	/**
	 * Finds the position of an item in the backpack grid.
	 */
	private Point findItemGridPosition(BackPack bp, Item item) {
		return GameView.findItemGridPosition(bp, item);
	}

	@Override
	public void handlePointerEvent(PointerEvent event) {
		if (event.action() != PointerEvent.Action.POINTER_DOWN || lastMetrics == null)
			return;
		int mx = (int) event.location().x();
		int my = (int) event.location().y();

		if (handleShopClick(mx, my))
			return;
		if (handleBuyButtonClick(mx, my))
			return;
		handleBackpackClick(mx, my);
	}

	/**
	 * Handles clicks within the merchant's list of items.
	 */
	private boolean handleShopClick(int mx, int my) {
		Metrics m = lastMetrics;
		List<Item> shopItems = merchant.getShop();
		for (int i = 0; i < shopItems.size(); i++) {
			int y = m.startY_Shop + i * (m.shopItemH + 10);
			if (mx >= m.startX_Shop && mx <= m.startX_Shop + m.shopItemW && my >= y && my <= y + m.shopItemH) {
				selectedShopItem = i;
				selectedBackpackItem = null;
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles clicks on the "Buy" button.
	 */
	private boolean handleBuyButtonClick(int mx, int my) {
		if (buyButtonRect != null && buyButtonRect.contains(mx, my) && selectedShopItem != -1) {
			buyItem(selectedShopItem);
			return true;
		}
		return false;
	}

	/**
	 * Handles clicks within the hero's backpack grid.
	 */
	private void handleBackpackClick(int mx, int my) {
		Metrics m = lastMetrics;
		BackPack bp = hero.getBackPack();
		int gx = (mx - m.startX_BP) / m.cellSize_BP;
		int gy = (my - m.startY_BP) / m.cellSize_BP;
		if (gx >= 0 && gx < bp.getWidth() && gy >= 0 && gy < bp.getHeight()) {
			selectedBackpackItem = bp.getItemAt(gx, gy);
			if (selectedBackpackItem != null)
				selectedShopItem = -1;
		}
	}

	/**
	 * Processes the purchase of an item from the merchant.
	 */
	private void buyItem(int index) {
		int price = merchant.getPrice(index);
		int currentTotalGold = hero.getGold();

		// Initial funds check
		if (currentTotalGold < price) {
			fadingMessage = "Not enough gold!";
			fadingTimer = 100;
			return;
		}

		BackPack bp = hero.getBackPack();

		// Step 1: Retrieve and remove existing gold
		Item goldItem = findAndRemoveGold(bp);

		// Step 2: Calculate and give change (if necessary)
		handleChange(bp, currentTotalGold, price, goldItem);

		// Step 3: Attempt final purchase
		finalizePurchase(bp, index);
	}

	/**
	 * Scans inventory to find Gold item and removes it.
	 */
	private Item findAndRemoveGold(BackPack bp) {
		Item foundGold = null;
		for (Item item : bp.getItems()) {
			switch (item) {
				case item.Gold g -> {
					foundGold = g;
				}
				default -> {
					/* Ignore other items */ }
			}

			if (foundGold != null)
				break;
		}

		if (foundGold != null) {
			bp.completelyRemoveItem(foundGold);
		}

		return foundGold;
	}

	/**
	 * Calculates remaining cost and adds change to backpack.
	 */
	private void handleChange(BackPack bp, int currentTotalGold, int price, Item oldGold) {
		int remaining = currentTotalGold - price;

		if (remaining > 0) {
			Shape shape = (oldGold != null) ? oldGold.shape() : new Shape(new boolean[][] { { true } });

			item.Gold change = new item.Gold(
					0, // id
					"Gold Pouch", // name
					remaining, // amount
					false, // isSellable
					Rarity.COMMON, // rarity
					shape // shape
			);

			bp.add(change);
		}
	}

	/**
	 * Finalizes interaction with merchant and updates messages.
	 */
	private void finalizePurchase(BackPack bp, int index) {
		boolean success = merchant.buy(bp, index, 0, 0);

		if (success) {
			fadingMessage = "Bought!";
			fadingTimer = 100;
			selectedShopItem = -1;
		} else {
			fadingMessage = "Inventory full!";
			fadingTimer = 100;
		}
	}

	/**
	 * Sells the selected item from the backpack back to the merchant.
	 */
	private void sellItem() {
		if (selectedBackpackItem != null) {
			int price = merchant.sell(hero.getBackPack(), selectedBackpackItem);
			if (price > 0) {
				hero.addGold(price);
				selectedBackpackItem = null;
			}
		}
	}

	@Override
	public void handleKeyBoardEvent(KeyboardEvent event) {
		if (event.action() != KeyboardEvent.Action.KEY_PRESSED)
			return;
		switch (event.key()) {
			case KeyboardEvent.Key.SPACE -> done = true;
			case KeyboardEvent.Key.S -> sellItem();
			case KeyboardEvent.Key.ESCAPE -> done = true;
			default -> {
			}
		}
	}

	@Override
	public void updateLogic() {
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public GameView nextView() {
		return parentView;
	}
}
