import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameWorld {
    private Player player;
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<EnemyBullet> enemyBullets = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();
    private final ArrayList<Bunker> bunkers = new ArrayList<>();
    private Ufo ufo;
    private final ConcurrentLinkedQueue<Bullet> pendingBullets = new ConcurrentLinkedQueue<>();

    private int score = 0;
    private int creditsEarned = 0;
    private int level = 1;
    private int enemyDirection = 1;
    private int enemyMoveCooldown = 0;
    private int enemyMoveDelay = 30;
    private int enemyShootCooldown = 0;
    private int enemiesPerWave = 0;
    private int ufoSpawnCooldown = 0;
    private int rapidFireTimer = 0;
    private int shieldTimer = 0;
    private int messageTimer = 0;
    private String powerUpMessage = "";
    private Color powerUpMessageColor = Color.WHITE;
    private int respawnTimer = 0;
    private boolean playerVisible = true;
    private boolean invaderFrame = false;

    private boolean moveLeftPressed = false;
    private boolean moveRightPressed = false;
    private boolean shootPressed = false;

    public void startNewGame(ShipType shipType) {
        player = new Player(380, 520, shipType);
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        bunkers.clear();
        ufo = null;
        SoundSystem.stopUfoLoop();
        pendingBullets.clear();
        SoundSystem.stopUfoLoop();

        score = 0;
        creditsEarned = 0;
        level = 1;
        invaderFrame = false;
        Enemy.setAltFrame(false);
        moveLeftPressed = false;
        moveRightPressed = false;
        shootPressed = false;
        rapidFireTimer = 0;
        shieldTimer = 0;
        messageTimer = 0;
        respawnTimer = 0;
        playerVisible = true;

        startLevel();
    }

    public void update() {
        Bullet pending;
        while ((pending = pendingBullets.poll()) != null) {
            bullets.add(pending);
        }

        if (moveLeftPressed) player.moveLeft();
        if (moveRightPressed) player.moveRight();
        if (shootPressed) {
            if (bullets.size() < player.getMaxBullets()) {
                ArrayList<Bullet> shots = player.shoot();
                if (shots != null && !shots.isEmpty()) {
                    pendingBullets.addAll(shots);
                    SoundSystem.playPlayerShoot();
                }
            }
        }

        player.update();
        Bullet burst = player.pollBurstShot();
        if (burst != null) {
            pendingBullets.add(burst);
            SoundSystem.playPlayerShoot();
        }
        enemies.forEach(Enemy::update);
        if (ufo != null) ufo.update();
        bullets.forEach(Bullet::update);
        enemyBullets.forEach(EnemyBullet::update);
        powerUps.forEach(PowerUp::update);
        bunkers.forEach(Bunker::update);

        if (rapidFireTimer > 0) {
            rapidFireTimer--;
            if (rapidFireTimer == 0) player.resetFireRate();
        }
        if (shieldTimer > 0) shieldTimer--;
        if (messageTimer > 0) messageTimer--;
        if (respawnTimer > 0) {
            respawnTimer--;
            playerVisible = (respawnTimer % 6) < 3;
            if (respawnTimer == 0) playerVisible = true;
        }

        updateEnemiesMovement();
        enemyShooting();
        updateUfo();
        checkCollisions();
    }

    private void startLevel() {
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        powerUps.clear();
        bunkers.clear();
        ufo = null;

        enemyDirection = 1;
        invaderFrame = false;
        Enemy.setAltFrame(false);
        enemyMoveDelay = Math.max(8, 28 - level * 2);
        enemyMoveCooldown = enemyMoveDelay;
        enemyShootCooldown = Math.max(20, 60 - level * 3);

        int rows = 5;
        int cols = 10;
        int startX = 140;
        int startY = 60;
        int gapX = 50;
        int gapY = 35;

        for (int r = 0; r < rows; r++) {
            Enemy.EnemyType type;
            if (r == 0) {
                type = Enemy.EnemyType.SMALL;
            } else if (r == 1 || r == 2) {
                type = Enemy.EnemyType.MEDIUM;
            } else {
                type = Enemy.EnemyType.LARGE;
            }
            for (int c = 0; c < cols; c++) {
                enemies.add(new Enemy(startX + c * gapX, startY + r * gapY, type));
            }
        }

        enemiesPerWave = rows * cols;
        createBunkers();
    }

    private void updateEnemiesMovement() {
        if (enemies.isEmpty()) {
            if (player.hp < 3) player.hp++;
            level++;
            startLevel();
            return;
        }

        enemyMoveCooldown--;
        if (enemyMoveCooldown > 0) return;

        int alive = enemies.size();
        int speedBoost = (enemiesPerWave - alive) / 4;
        enemyMoveDelay = Math.max(6, 30 - level * 2 - speedBoost);
        enemyMoveCooldown = enemyMoveDelay;

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (Enemy e : enemies) {
            minX = Math.min(minX, e.x);
            maxX = Math.max(maxX, e.x + e.width);
        }

        int dx = enemyDirection * 8;
        int dy = 0;
        if (maxX + dx >= 780 || minX + dx <= 0) {
            enemyDirection *= -1;
            dx = 0;
            dy = 12;
        }

        for (Enemy e : enemies) {
            e.moveBy(dx, dy);
        }
        invaderFrame = !invaderFrame;
        Enemy.setAltFrame(invaderFrame);
        SoundSystem.playStep(invaderFrame);
    }

    private void enemyShooting() {
        if (enemyShootCooldown > 0) {
            enemyShootCooldown--;
            return;
        }

        enemyShootCooldown = Math.max(18, 60 - level * 3);

        if (enemyBullets.size() >= 3) return;
        Enemy shooter = pickBottomShooter();
        if (shooter != null) {
            enemyBullets.add(new EnemyBullet(shooter.x + shooter.width / 2, shooter.y + shooter.height));
        }
    }

    private Enemy pickBottomShooter() {
        ArrayList<Enemy> candidates = new ArrayList<>();
        for (Enemy e : enemies) {
            boolean blocked = false;
            for (Enemy other : enemies) {
                if (other != e && Math.abs(other.x - e.x) < e.width && other.y > e.y) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) candidates.add(e);
        }
        if (candidates.isEmpty()) return null;
        return candidates.get((int) (Math.random() * candidates.size()));
    }

    private void updateUfo() {
        if (ufo == null) {
            if (ufoSpawnCooldown > 0) {
                ufoSpawnCooldown--;
                return;
            }
            if (Math.random() < 0.003) {
                int speed = Math.random() < 0.5 ? 3 : -3;
                int startX = speed > 0 ? -60 : 860;
                ufo = new Ufo(startX, 40, speed, 100);
                ufoSpawnCooldown = 400;
                SoundSystem.startUfoLoop();
            }
            return;
        }

        if (ufo.x > 900 || ufo.x + ufo.width < -100) {
            ufo = null;
            SoundSystem.stopUfoLoop();
        }
    }

    private void createBunkers() {
        int startY = 440;
        int startX = 90;
        int gap = 170;
        for (int i = 0; i < 4; i++) {
            bunkers.add(new Bunker(startX + i * gap, startY));
        }
    }

    private void checkCollisions() {
        ArrayList<Enemy> deadEnemies = new ArrayList<>();
        ArrayList<Bullet> deadBullets = new ArrayList<>();
        ArrayList<EnemyBullet> deadEnemyBullets = new ArrayList<>();

        for (Bullet b : bullets) {
            for (Enemy e : enemies) {
                if (b.getBounds().intersects(e.getBounds())) {
                    e.hp--;
                    deadBullets.add(b);
                    if (e.hp <= 0) {
                        deadEnemies.add(e);
                        SoundSystem.playEnemyHit();
                        score += e.getScoreValue();
                        creditsEarned += 5;
                        if (Math.random() < 0.2) {
                            powerUps.add(new PowerUp(
                                    e.x,
                                    e.y,
                                    PowerUpType.values()[(int) (Math.random() * PowerUpType.values().length)]
                            ));
                        }
                    }
                }
            }
            for (Bunker bunker : bunkers) {
                if (bunker.hit(b.getBounds())) {
                    deadBullets.add(b);
                    break;
                }
            }
            if (ufo != null && b.getBounds().intersects(ufo.getBounds())) {
                score += ufo.getScoreValue();
                deadBullets.add(b);
                ufo = null;
                SoundSystem.stopUfoLoop();
            }
        }

        enemies.removeAll(deadEnemies);
        bullets.removeAll(deadBullets);

        for (EnemyBullet eb : enemyBullets) {
            if (eb.getBounds().intersects(player.getBounds())) {
                if (shieldTimer <= 0) {
                    player.hp -= 1;
                    respawnTimer = 60;
                    SoundSystem.playPlayerHit();
                }
                deadEnemyBullets.add(eb);
                continue;
            }
            for (Bunker bunker : bunkers) {
                if (bunker.hit(eb.getBounds())) {
                    deadEnemyBullets.add(eb);
                    break;
                }
            }
        }

        enemyBullets.removeAll(deadEnemyBullets);
        bullets.removeIf(b -> b.y + b.height < 0);
        enemyBullets.removeIf(b -> b.y > 600);
        bunkers.removeIf(Bunker::isDestroyed);

        powerUps.removeIf(p -> {
            if (p.getBounds().intersects(player.getBounds())) {
                applyPowerUp(p);
                return true;
            }
            return false;
        });
    }

    private void applyPowerUp(PowerUp p) {
        switch (p.getType()) {
            case RAPID_FIRE:
                rapidFireTimer = 240;
                player.rapidFire();
                powerUpMessage = "";
                messageTimer = 0;
                break;
            case SHIELD:
                shieldTimer = 240;
                powerUpMessage = "";
                messageTimer = 0;
                break;
            case CREDITS:
                creditsEarned += 20;
                powerUpMessage = "Credits +20";
                messageTimer = 120;
                powerUpMessageColor = Color.YELLOW;
                break;
        }
    }

    public Player getPlayer() { return player; }
    public ArrayList<Enemy> getEnemies() { return enemies; }
    public ArrayList<Bullet> getBullets() { return bullets; }
    public ArrayList<EnemyBullet> getEnemyBullets() { return enemyBullets; }
    public ArrayList<PowerUp> getPowerUps() { return powerUps; }
    public ArrayList<Bunker> getBunkers() { return bunkers; }
    public Ufo getUfo() { return ufo; }
    public int getScore() { return score; }
    public int getCreditsEarned() { return creditsEarned; }
    public int getLevel() { return level; }
    public int getRapidFireTimer() { return rapidFireTimer; }
    public int getShieldTimer() { return shieldTimer; }
    public int getMessageTimer() { return messageTimer; }
    public String getPowerUpMessage() { return powerUpMessage; }
    public Color getPowerUpMessageColor() { return powerUpMessageColor; }
    public boolean isPlayerVisible() { return playerVisible; }

    public void setMoveLeftPressed(boolean pressed) { moveLeftPressed = pressed; }
    public void setMoveRightPressed(boolean pressed) { moveRightPressed = pressed; }
    public void setShootPressed(boolean pressed) { shootPressed = pressed; }
}
