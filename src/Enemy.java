import java.awt.*;

public class Enemy extends Entity {

    public enum EnemyType {
        SMALL,
        MEDIUM,
        LARGE
    }

    private static boolean altFrame = false;

    private final EnemyType type;
    private final int scoreValue;

    public Enemy(int x, int y, EnemyType type) {

        this.x = x;
        this.y = y;
        this.type = type;

        width = 30;
        height = 20;

        switch (type) {
            case SMALL:
                hp = 1;
                scoreValue = 30;
                break;
            case MEDIUM:
                hp = 1;
                scoreValue = 20;
                break;
            case LARGE:
            default:
                hp = 1;
                scoreValue = 10;
                break;
        }
    }

    public EnemyType getType() {
        return type;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public static void setAltFrame(boolean alt) {
        altFrame = alt;
    }

    public void moveBy(int dx, int dy) {
        x += dx;
        y += dy;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        switch (type) {
            case SMALL:
                drawPixelSprite(g, x, y, width, height, altFrame ? ENEMY_SPRITE_SMALL_ALT : ENEMY_SPRITE_SMALL);
                break;
            case MEDIUM:
                drawPixelSprite(g, x, y, width, height, altFrame ? ENEMY_SPRITE_MEDIUM_ALT : ENEMY_SPRITE_MEDIUM);
                break;
            case LARGE:
                drawPixelSprite(g, x, y, width, height, altFrame ? ENEMY_SPRITE_LARGE_ALT : ENEMY_SPRITE_LARGE);
                break;
        }
    }

    private static final String[] ENEMY_SPRITE_SMALL = {
            "0010001000",
            "0001110000",
            "0011111000",
            "0111111100",
            "1111111110",
            "1011111010",
            "0010001000",
            "0100000100"
    };

    private static final String[] ENEMY_SPRITE_SMALL_ALT = {
            "0010001000",
            "0001110000",
            "0011111000",
            "0111111100",
            "1111111110",
            "0011111000",
            "0100000100",
            "1000000010"
    };

    private static final String[] ENEMY_SPRITE_MEDIUM = {
            "0011111100",
            "0111111110",
            "1110110111",
            "1111111111",
            "1011111101",
            "1010000101",
            "0011111100",
            "0100000010"
    };

    private static final String[] ENEMY_SPRITE_MEDIUM_ALT = {
            "0011111100",
            "0111111110",
            "1110110111",
            "1111111111",
            "1011111101",
            "0010000100",
            "0101111010",
            "1000000001"
    };

    private static final String[] ENEMY_SPRITE_LARGE = {
            "0001111000",
            "0011111100",
            "0110110110",
            "1111111111",
            "1111111111",
            "1011111101",
            "0010110100",
            "0100000010"
    };

    private static final String[] ENEMY_SPRITE_LARGE_ALT = {
            "0001111000",
            "0011111100",
            "0110110110",
            "1111111111",
            "1111111111",
            "0011111000",
            "0100110010",
            "1000000001"
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
