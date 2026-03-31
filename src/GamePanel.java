import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumMap;

public class GamePanel extends JPanel implements Runnable {

    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;

    private Thread gameThread;
    private boolean running = false;
    private final EnumMap<GameState, StateHandler> stateHandlers = new EnumMap<>(GameState.class);
    private final GameWorld world = new GameWorld();

    private GameState gameState = GameState.MENU;
    private GameMode gameMode;

    private Rectangle menuStartButton;
    private Rectangle menuShopButton;
    private Rectangle menuResetButton;
    private Rectangle menuHowToButton;
    private Rectangle menuExitButton;
    private Rectangle shopScoutButton;
    private Rectangle shopFighterButton;
    private Rectangle shopDefaultButton;
    private Rectangle shopDestroyerButton;
    private Rectangle shopBackButton;
    private Rectangle pauseResumeButton;
    private Rectangle pauseMenuButton;
    private Rectangle gameOverMenuButton;
    private Rectangle howToBackButton;
    private boolean newHighScore = false;
    private static final int MENU_STAR_COUNT = 120;
    private final int[] menuStarX = new int[MENU_STAR_COUNT];
    private final int[] menuStarY = new int[MENU_STAR_COUNT];
    private final int[] menuStarSize = new int[MENU_STAR_COUNT];

    public GamePanel() {

        SaveSystem.load();

        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleInput(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseClick(e);
            }
        });

        initStateHandlers();
        initMenuStars();
    }

    private void initMenuStars() {
        for (int i = 0; i < MENU_STAR_COUNT; i++) {
            menuStarX[i] = (int) (Math.random() * BASE_WIDTH);
            menuStarY[i] = (int) (Math.random() * BASE_HEIGHT);
            menuStarSize[i] = 1 + (int) (Math.random() * 2);
        }
    }

    private void initStateHandlers() {
        stateHandlers.put(GameState.MENU, new MenuState(this));
        stateHandlers.put(GameState.SHOP, new ShopState(this));
        stateHandlers.put(GameState.HOW_TO, new HowToState(this));
        stateHandlers.put(GameState.PLAYING, new PlayingState(this));
        stateHandlers.put(GameState.PAUSED, new PausedState(this));
        stateHandlers.put(GameState.GAME_OVER, new GameOverState(this));
    }

    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        while (running) {

            stateHandlers.get(gameState).update();

            repaint();

            try { Thread.sleep(16); }
            catch (InterruptedException ignored) {}
        }
    }

    private void startNewGame(GameMode mode) {

        this.gameMode = mode;
        world.startNewGame(SaveSystem.ownedShip);
        newHighScore = false;
        gameState = GameState.PLAYING;
        requestFocusInWindow();
    }

    void updateGame() {
        world.update();
        checkGameOver();
    }

    private void checkGameOver() {
        if (world.getPlayer().hp <= 0) {
            finishGame(true);
        }

        for (Enemy e : world.getEnemies()) {
            if (e.y + e.height >= world.getPlayer().y) {
                finishGame(false);
                break;
            }
        }
    }

    private void finishGame(boolean playerDied) {
        if (gameState == GameState.GAME_OVER) return;

        SaveSystem.credits += world.getCreditsEarned();

        newHighScore = world.getScore() > SaveSystem.highScore;
        if (newHighScore)
            SaveSystem.highScore = world.getScore();

        SaveSystem.save();
        if (playerDied) SoundSystem.playPlayerDeath();
        gameState = GameState.GAME_OVER;
    }

    private void handleInput(KeyEvent e) {
        stateHandlers.get(gameState).keyPressed(e);
    }

    private void handleKeyRelease(KeyEvent e) {
        stateHandlers.get(gameState).keyReleased(e);
    }

    void handlePlayingKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) world.setMoveLeftPressed(true);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) world.setMoveRightPressed(true);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) world.setShootPressed(true);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameState = GameState.PAUSED;
    }

    void handlePlayingKeyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) world.setMoveLeftPressed(false);
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) world.setMoveRightPressed(false);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) world.setShootPressed(false);
    }

    void handlePausedKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) gameState = GameState.PLAYING;
    }

    private void handleMouseClick(MouseEvent e) {
        Point p = toVirtualPoint(e.getPoint());
        stateHandlers.get(gameState).mouseClick(p);
    }

    private void purchaseShip(ShipType type, int cost) {
        int bit = 1 << type.ordinal();
        boolean owned = (SaveSystem.ownedMask & bit) != 0;
        if (!owned) {
            if (SaveSystem.credits < cost) return;
            SaveSystem.credits -= cost;
            SaveSystem.ownedMask |= bit;
        }
        SaveSystem.ownedShip = type;
        SaveSystem.save();
    }

    private void goToMenu() {
        world.setMoveLeftPressed(false);
        world.setMoveRightPressed(false);
        world.setShootPressed(false);
        gameState = GameState.MENU;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        int panelW = getWidth();
        int panelH = getHeight();
        double scale = Math.min(panelW / (double) BASE_WIDTH, panelH / (double) BASE_HEIGHT);
        int offsetX = (int) Math.round((panelW - BASE_WIDTH * scale) / 2.0);
        int offsetY = (int) Math.round((panelH - BASE_HEIGHT * scale) / 2.0);
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        menuStartButton = null;
        menuShopButton = null;
        menuResetButton = null;
        menuHowToButton = null;
        menuExitButton = null;
        shopScoutButton = null;
        shopFighterButton = null;
        shopDefaultButton = null;
        shopDestroyerButton = null;
        shopBackButton = null;
        pauseResumeButton = null;
        pauseMenuButton = null;
        gameOverMenuButton = null;
        howToBackButton = null;

        stateHandlers.get(gameState).draw(g);
    }

    void drawMenu(Graphics g) {
        drawMenuBackground((Graphics2D) g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredText(g, "SPACE INVADERS", 200);

        menuStartButton = new Rectangle(280, 240, 240, 40);
        menuShopButton = new Rectangle(280, 295, 240, 40);
        menuHowToButton = new Rectangle(280, 350, 240, 40);
        menuResetButton = new Rectangle(280, 405, 240, 40);
        menuExitButton = new Rectangle(280, 460, 240, 40);
        drawButton(g, menuStartButton, "Start Game");
        drawButton(g, menuShopButton, "Shop");
        drawButton(g, menuHowToButton, "How To Play");
        drawButton(g, menuResetButton, "Reset Data");
        drawButton(g, menuExitButton, "Exit");

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        drawCenteredText(g, "High Score: " + SaveSystem.highScore, 520);
    }

    private void drawMenuBackground(Graphics2D g2) {
        Paint oldPaint = g2.getPaint();
        Composite oldComposite = g2.getComposite();

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(6, 10, 26),
                0, BASE_HEIGHT, new Color(0, 0, 0)
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, BASE_WIDTH, BASE_HEIGHT);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
        g2.setColor(new Color(80, 120, 200));
        g2.fillOval(20, 40, 220, 140);
        g2.setColor(new Color(120, 90, 180));
        g2.fillOval(520, 20, 260, 180);
        g2.setColor(new Color(40, 140, 160));
        g2.fillOval(120, 380, 320, 200);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
        for (int i = 0; i < MENU_STAR_COUNT; i++) {
            int size = menuStarSize[i];
            g2.setColor(size == 1 ? new Color(180, 200, 255) : new Color(240, 240, 255));
            g2.fillRect(menuStarX[i], menuStarY[i], size, size);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        boolean alt = (System.currentTimeMillis() / 400) % 2 == 0;
        g2.setColor(Color.WHITE);
        drawPixelSprite(g2, 600, 100, 36, 24, alt ? MENU_INVADER_MEDIUM_ALT : MENU_INVADER_MEDIUM);
        drawPixelSprite(g2, 650, 150, 34, 22, alt ? MENU_INVADER_SMALL_ALT : MENU_INVADER_SMALL);
        g2.setColor(new Color(0, 170, 220));
        drawPixelSprite(g2, 598, 395, 40, 20, MENU_SCOUT);
        g2.setColor(new Color(255, 220, 120));
        g2.fillRect(616, 245, 4, 20);

        g2.setComposite(oldComposite);
        g2.setPaint(oldPaint);
    }

    private static final String[] MENU_INVADER_SMALL = {
            "0010001000",
            "0001110000",
            "0011111000",
            "0111111100",
            "1111111110",
            "1011111010",
            "0010001000",
            "0100000100"
    };

    private static final String[] MENU_INVADER_SMALL_ALT = {
            "0010001000",
            "0001110000",
            "0011111000",
            "0111111100",
            "1111111110",
            "0011111000",
            "0100000100",
            "1000000010"
    };

    private static final String[] MENU_INVADER_MEDIUM = {
            "0011111100",
            "0111111110",
            "1110110111",
            "1111111111",
            "1011111101",
            "1010000101",
            "0011111100",
            "0100000010"
    };

    private static final String[] MENU_INVADER_MEDIUM_ALT = {
            "0011111100",
            "0111111110",
            "1110110111",
            "1111111111",
            "1011111101",
            "0010000100",
            "0101111010",
            "1000000001"
    };

    private static final String[] MENU_SCOUT = {
            "0001100000",
            "0011110000",
            "0111111000",
            "1111111100",
            "1111111100",
            "0010010000",
            "0100001000",
            "1000000100"
    };

    void drawShop(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        drawCenteredText(g, "SHOP", 150);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredText(g, "Credits: " + SaveSystem.credits, 200);

        shopDefaultButton = new Rectangle(260, 230, 280, 40);
        shopFighterButton = new Rectangle(260, 280, 280, 40);
        shopScoutButton = new Rectangle(260, 330, 280, 40);
        shopDestroyerButton = new Rectangle(260, 380, 280, 40);
        shopBackButton = new Rectangle(320, 450, 160, 36);

        drawButton(g, shopDefaultButton, shipLabel("Default", 0, ShipType.DEFAULT));
        drawButton(g, shopFighterButton, shipLabel("Fighter", 500, ShipType.FIGHTER));
        drawButton(g, shopScoutButton, shipLabel("Scout", 1000, ShipType.SCOUT));
        drawButton(g, shopDestroyerButton, shipLabel("Destroyer", 2000, ShipType.DESTROYER));
        drawButton(g, shopBackButton, "Back");
    }

    void drawPlaying(Graphics g, boolean paused) {
        Player player = world.getPlayer();
        ArrayList<Enemy> enemies = world.getEnemies();
        ArrayList<Bullet> bullets = world.getBullets();
        ArrayList<EnemyBullet> enemyBullets = world.getEnemyBullets();
        ArrayList<PowerUp> powerUps = world.getPowerUps();
        ArrayList<Bunker> bunkers = world.getBunkers();
        Ufo ufo = world.getUfo();

        if (world.isPlayerVisible()) player.draw(g);
        if (world.getShieldTimer() > 0) {
            Rectangle r = player.getSpriteBounds();
            int pad = 8;
            int sx = r.x - pad;
            int sy = r.y - pad;
            int sw = r.width + pad * 2;
            int sh = r.height + pad * 2;
            g.setColor(new Color(80, 160, 255));
            g.drawOval(sx, sy, sw, sh);
        }
        enemies.forEach(e -> e.draw(g));
        bullets.forEach(b -> b.draw(g));
        enemyBullets.forEach(b -> b.draw(g));
        powerUps.forEach(p -> p.draw(g));
        bunkers.forEach(b -> b.draw(g));
        if (ufo != null) ufo.draw(g);

        g.setColor(Color.WHITE);
        Font oldHud = g.getFont();
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Level: " + world.getLevel(), 10, 24);
        g.drawString("Score: " + world.getScore(), 10, 48);
        g.drawString("Credits: " + (SaveSystem.credits + world.getCreditsEarned()), 10, 72);
        drawLives(g, 12);
        g.setFont(oldHud);

        if (world.getRapidFireTimer() > 0 || world.getShieldTimer() > 0) {
            Font old = g.getFont();
            g.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            String status = "";
            if (world.getRapidFireTimer() > 0) status += "Rapid Fire " + formatTime(world.getRapidFireTimer());
            if (world.getShieldTimer() > 0) status += (status.isEmpty() ? "" : "  ") + "Shield " + formatTime(world.getShieldTimer());
            int ty = 28;
            int x = (BASE_WIDTH - fm.stringWidth(status)) / 2;
            if (world.getRapidFireTimer() > 0) {
                String text = "Rapid Fire " + formatTime(world.getRapidFireTimer());
                g.setColor(Color.CYAN);
                g.drawString(text, x, ty);
                x += fm.stringWidth(text);
                if (world.getShieldTimer() > 0) x += fm.stringWidth("  ");
            }
            if (world.getShieldTimer() > 0) {
                String text = "Shield " + formatTime(world.getShieldTimer());
                g.setColor(Color.BLUE);
                g.drawString(text, x, ty);
            }
            g.setFont(old);
        }

        if (world.getMessageTimer() > 0 && !world.getPowerUpMessage().isEmpty()) {
            Font old = g.getFont();
            g.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g.getFontMetrics();
            int tx = (BASE_WIDTH - fm.stringWidth(world.getPowerUpMessage())) / 2;
            g.setColor(world.getPowerUpMessageColor());
            g.drawString(world.getPowerUpMessage(), tx, 55);
            g.setFont(old);
        }

        if (paused) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("PAUSED", 330, 300);

            pauseResumeButton = new Rectangle(300, 340, 200, 40);
            pauseMenuButton = new Rectangle(300, 390, 200, 40);
            drawButton(g, pauseResumeButton, "Resume");
            drawButton(g, pauseMenuButton, "Menu");
        }
    }

    void drawGameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        drawCenteredText(g, "GAME OVER", 250);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        drawCenteredText(g, "Score: " + world.getScore(), 300);
        drawCenteredText(g, "Credits earned: " + world.getCreditsEarned(), 330);
        if (newHighScore) {
            g.setColor(Color.YELLOW);
            drawCenteredText(g, "NEW HIGH SCORE!", 360);
            g.setColor(Color.WHITE);
        }
        gameOverMenuButton = new Rectangle(300, 380, 200, 40);
        drawButton(g, gameOverMenuButton, "Menu");
    }

    void drawHowTo(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        drawCenteredText(g, "HOW TO PLAY", 140);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        int y = 200;
        int textX = 220;
        g.drawString("- Move: LEFT / RIGHT arrows", textX, y); y += 26;
        g.drawString("- Shoot: SPACE (limit depends on ship)", textX, y); y += 26;
        g.drawString("- Clear all invaders to advance levels", textX, y); y += 26;
        g.drawString("- Bonus life after each level (max 3)", textX, y); y += 26;
        g.drawString("- Power-ups: Cyan=Rapid Fire, Blue=Shield", textX, y); y += 26;
        g.drawString("- Power-ups: Yellow=Credits", textX, y); y += 26;
        g.drawString("- UFO gives bonus score", textX, y); y += 26;

        howToBackButton = new Rectangle(320, 430, 160, 36);
        drawButton(g, howToBackButton, "Back");
    }

    void handleMenuClick(Point p) {
        if (menuStartButton != null && menuStartButton.contains(p)) {
            startNewGame(GameMode.ENDLESS);
            return;
        }
        if (menuShopButton != null && menuShopButton.contains(p)) {
            gameState = GameState.SHOP;
            return;
        }
        if (menuHowToButton != null && menuHowToButton.contains(p)) {
            gameState = GameState.HOW_TO;
            return;
        }
        if (menuResetButton != null && menuResetButton.contains(p)) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "Reset all progress? This cannot be undone.",
                    "Confirm Reset",
                    JOptionPane.YES_NO_OPTION
            );
            if (result == JOptionPane.YES_OPTION) {
                SaveSystem.reset();
            }
            return;
        }
        if (menuExitButton != null && menuExitButton.contains(p)) {
            System.exit(0);
        }
    }

    void handleShopClick(Point p) {
        if (shopDefaultButton != null && shopDefaultButton.contains(p)) {
            purchaseShip(ShipType.DEFAULT, 0);
        } else if (shopFighterButton != null && shopFighterButton.contains(p)) {
            purchaseShip(ShipType.FIGHTER, 500);
        } else if (shopScoutButton != null && shopScoutButton.contains(p)) {
            purchaseShip(ShipType.SCOUT, 1000);
        } else if (shopDestroyerButton != null && shopDestroyerButton.contains(p)) {
            purchaseShip(ShipType.DESTROYER, 2000);
        } else if (shopBackButton != null && shopBackButton.contains(p)) {
            goToMenu();
        }
    }

    void handlePausedClick(Point p) {
        if (pauseResumeButton != null && pauseResumeButton.contains(p)) {
            gameState = GameState.PLAYING;
        } else if (pauseMenuButton != null && pauseMenuButton.contains(p)) {
            goToMenu();
        }
    }

    void handleGameOverClick(Point p) {
        if (gameOverMenuButton != null && gameOverMenuButton.contains(p)) {
            goToMenu();
        }
    }

    void handleHowToClick(Point p) {
        if (howToBackButton != null && howToBackButton.contains(p)) {
            goToMenu();
        }
    }


    private void drawButton(Graphics g, Rectangle r, String label) {
        g.setColor(new Color(40, 40, 40));
        g.fillRect(r.x, r.y, r.width, r.height);
        g.setColor(Color.WHITE);
        g.drawRect(r.x, r.y, r.width, r.height);

        Font old = g.getFont();
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(label, tx, ty);
        g.setFont(old);
    }

    private void drawCenteredText(Graphics g, String text, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (BASE_WIDTH - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void drawLives(Graphics g, int y) {
        Player player = world.getPlayer();
        int lives = Math.max(0, player.hp);
        if (lives == 0) return;
        String[] sprite = player.getSpriteMask();
        int iconW = 32;
        int iconH = 16;
        int gap = 8;
        int totalW = lives * iconW + (lives - 1) * gap;
        int x = BASE_WIDTH - 10 - totalW;
        g.setColor(player.getShipColor());
        for (int i = 0; i < lives; i++) {
            int px = x + i * (iconW + gap);
            drawPixelSprite(g, px, y, iconW, iconH, sprite);
        }
    }

    private void drawPixelSprite(Graphics g, int x, int y, int w, int h, String[] sprite) {
        int rows = sprite.length;
        int cols = sprite[0].length();
        int cellW = Math.max(1, w / cols);
        int cellH = Math.max(1, h / rows);
        int padX = (w - cellW * cols) / 2;
        int padY = (h - cellH * rows) / 2;

        for (int r = 0; r < rows; r++) {
            String row = sprite[r];
            for (int c = 0; c < cols; c++) {
                if (row.charAt(c) == '1') {
                    g.fillRect(x + padX + c * cellW, y + padY + r * cellH, cellW, cellH);
                }
            }
        }
    }

    private String shipLabel(String name, int cost, ShipType type) {
        int bit = 1 << type.ordinal();
        boolean owned = (SaveSystem.ownedMask & bit) != 0;
        boolean selected = SaveSystem.ownedShip == type;
        String price = cost == 0 ? "Free" : String.valueOf(cost);
        String state = owned ? "Owned" : price;
        if (selected) {
            return name + " (Selected)";
        }
        return name + " (" + state + ")";
    }

    private String formatTime(int frames) {
        int seconds = (int) Math.ceil(frames / 60.0);
        return seconds + "s";
    }

    private Point toVirtualPoint(Point p) {
        int panelW = getWidth();
        int panelH = getHeight();
        double scale = Math.min(panelW / (double) BASE_WIDTH, panelH / (double) BASE_HEIGHT);
        int offsetX = (int) Math.round((panelW - BASE_WIDTH * scale) / 2.0);
        int offsetY = (int) Math.round((panelH - BASE_HEIGHT * scale) / 2.0);
        int vx = (int) Math.round((p.x - offsetX) / scale);
        int vy = (int) Math.round((p.y - offsetY) / scale);
        return new Point(vx, vy);
    }
}
