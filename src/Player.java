import java.awt.*;
import java.util.ArrayList;

public class Player extends Entity {

    private final ShipType type;
    private int speed;
    private int fireDelay = 0;
    private int baseFireRate;
    private int fireRate;
    private int bulletSpeed;
    private int maxBullets;
    private boolean rapidFireActive = false;
    private int burstPending = 0;
    private int burstTimer = 0;
    private int burstX = 0;
    private int burstY = 0;
    private int burstSpeed = 0;

    public Player(int x, int y, ShipType type) {

        this.x = x;
        this.y = y;
        this.type = type;

        width = 40;
        height = 20;

        switch (type) {
            case DEFAULT:
                speed = 4;
                baseFireRate = 26;
                fireRate = baseFireRate;
                bulletSpeed = 7;
                maxBullets = 1;
                hp = 3;
                break;
            case FIGHTER:
                speed = 6;
                baseFireRate = 20;
                fireRate = baseFireRate;
                bulletSpeed = 8;
                maxBullets = 1;
                hp = 3;
                break;
            case SCOUT:
                speed = 8;
                baseFireRate = 14;
                fireRate = baseFireRate;
                bulletSpeed = 9;
                maxBullets = 1;
                hp = 3;
                break;
            case DESTROYER:
                speed = 6;
                baseFireRate = 12;
                fireRate = baseFireRate;
                bulletSpeed = 8;
                maxBullets = 2;
                hp = 3;
                break;
        }
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }

    public void moveRight() {
        x += speed;
        if (x > 760) x = 760;
    }

    public ArrayList<Bullet> shoot() {
        if (fireDelay > 0 || burstPending > 0) return null;
        fireDelay = fireRate;
        ArrayList<Bullet> shots = new ArrayList<>();
        Rectangle r = getSpriteBounds();
        int center = r.x + r.width / 2 - 2;
        int speed = rapidFireActive ? bulletSpeed + 3 : bulletSpeed;
        if (type == ShipType.DESTROYER) {
            shots.add(new Bullet(center, y, speed));
            burstPending = 1;
            burstTimer = 4;
            burstX = center;
            burstY = y;
            burstSpeed = speed;
        } else {
            shots.add(new Bullet(center, y, speed));
        }
        return shots;
    }

    public void rapidFire() {
        rapidFireActive = true;
        fireRate = 1;
        fireDelay = 0;
    }

    public void heal() {
        hp = Math.min(hp + 1, 3);
    }

    public void resetFireRate() {
        fireRate = baseFireRate;
        rapidFireActive = false;
    }

    public ShipType getType() {
        return type;
    }

    public int getMaxBullets() {
        return maxBullets;
    }

    public Bullet pollBurstShot() {
        if (burstPending <= 0 || burstTimer > 0) return null;
        burstPending = 0;
        return new Bullet(burstX, burstY, burstSpeed);
    }

    public Rectangle getSpriteBounds() {
        String[] sprite = getActiveSprite();
        int rows = sprite.length;
        int cols = sprite[0].length();
        int cellW = Math.max(1, width / cols);
        int cellH = Math.max(1, height / rows);
        int padX = (width - cellW * cols) / 2;
        int padY = (height - cellH * rows) / 2;

        int minCol = cols;
        int maxCol = -1;
        int minRow = rows;
        int maxRow = -1;
        for (int r = 0; r < rows; r++) {
            String row = sprite[r];
            for (int c = 0; c < cols; c++) {
                if (row.charAt(c) == '1') {
                    if (c < minCol) minCol = c;
                    if (c > maxCol) maxCol = c;
                    if (r < minRow) minRow = r;
                    if (r > maxRow) maxRow = r;
                }
            }
        }

        if (maxCol < minCol || maxRow < minRow) {
            return new Rectangle(x + padX, y + padY, cellW * cols, cellH * rows);
        }

        int bx = x + padX + minCol * cellW;
        int by = y + padY + minRow * cellH;
        int bw = (maxCol - minCol + 1) * cellW;
        int bh = (maxRow - minRow + 1) * cellH;
        return new Rectangle(bx, by, bw, bh);
    }

    public String[] getSpriteMask() {
        return getActiveSprite();
    }

    public Color getShipColor() {
        switch (type) {
            case DEFAULT:
                return new Color(140, 140, 140);
            case FIGHTER:
                return new Color(0, 170, 220);
            case SCOUT:
                return new Color(0, 200, 0);
            case DESTROYER:
            default:
                return new Color(220, 140, 40);
        }
    }

    @Override
    public void update() {
        if (fireDelay > 0) {
            fireDelay -= rapidFireActive ? 2 : 1;
            if (fireDelay < 0) fireDelay = 0;
        }
        if (burstPending > 0 && burstTimer > 0) {
            burstTimer--;
        }
    }

    @Override
    public void draw(Graphics g) {
        Color body;
        switch (type) {
            case DEFAULT:
                body = new Color(140, 140, 140);
                break;
            case FIGHTER:
                body = new Color(0, 200, 0);
                break;
            case SCOUT:
                body = new Color(0, 170, 220);
                break;
            case DESTROYER:
            default:
                body = new Color(240, 180, 40);
                break;
        }
        g.setColor(body);
        drawPixelSprite(g, x, y, width, height, getActiveSprite());
    }

    private String[] getActiveSprite() {
        switch (type) {
            case DEFAULT:
                return PLAYER_SPRITE_DEFAULT;
            case FIGHTER:
                return PLAYER_SPRITE_FIGHTER;
            case SCOUT:
                return PLAYER_SPRITE_SCOUT;
            case DESTROYER:
            default:
                return PLAYER_SPRITE_DESTROYER;
        }
    }

    private static final String[] PLAYER_SPRITE_DEFAULT = {
            "0001100000",
            "0011110000",
            "0111111000",
            "1111111100",
            "1111111100",
            "0010010000",
            "0100001000",
            "0100001000"
    };

    private static final String[] PLAYER_SPRITE_SCOUT = {
            "0001100000",
            "0011110000",
            "0111111000",
            "1111111100",
            "1111111100",
            "0010010000",
            "0100001000",
            "1000000100"
    };

    private static final String[] PLAYER_SPRITE_FIGHTER = {
            "0001100000",
            "0011110000",
            "0111111000",
            "1111111100",
            "1111111100",
            "0111111000",
            "1100001100",
            "1000000100"
    };

    private static final String[] PLAYER_SPRITE_DESTROYER = {
            "0001100000",
            "0011110000",
            "0111111000",
            "1111111100",
            "1111111100",
            "0111111000",
            "1101101100",
            "1000000100"
    };

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
}
