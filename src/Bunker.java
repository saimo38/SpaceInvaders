import java.awt.*;

public class Bunker extends Entity {

    private static final String[] BUNKER_SPRITE = {
            "001111111100",
            "011111111110",
            "111111111111",
            "111111111111",
            "111111111111",
            "111110011111",
            "111100001111",
            "111000000111"
    };

    private final boolean[][] cells;
    private int remaining;

    public Bunker(int x, int y) {
        this.x = x;
        this.y = y;
        width = 90;
        height = 60;

        int rows = BUNKER_SPRITE.length;
        int cols = BUNKER_SPRITE[0].length();
        cells = new boolean[rows][cols];
        remaining = 0;
        for (int r = 0; r < rows; r++) {
            String row = BUNKER_SPRITE[r];
            for (int c = 0; c < cols; c++) {
                boolean on = row.charAt(c) == '1';
                cells[r][c] = on;
                if (on) remaining++;
            }
        }
    }

    public boolean hit(Rectangle r) {
        int rows = cells.length;
        int cols = cells[0].length;
        int cellW = Math.max(1, width / cols);
        int cellH = Math.max(1, height / rows);
        int padX = (width - cellW * cols) / 2;
        int padY = (height - cellH * rows) / 2;

        for (int rr = 0; rr < rows; rr++) {
            for (int cc = 0; cc < cols; cc++) {
                if (!cells[rr][cc]) continue;
                int bx = x + padX + cc * cellW;
                int by = y + padY + rr * cellH;
                if (r.intersects(bx, by, cellW, cellH)) {
                    cells[rr][cc] = false;
                    remaining--;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isDestroyed() {
        return remaining <= 0;
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(0, 200, 0));
        int rows = cells.length;
        int cols = cells[0].length;
        int cellW = Math.max(1, width / cols);
        int cellH = Math.max(1, height / rows);
        int padX = (width - cellW * cols) / 2;
        int padY = (height - cellH * rows) / 2;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!cells[r][c]) continue;
                g.fillRect(x + padX + c * cellW, y + padY + r * cellH, cellW, cellH);
            }
        }
    }
}
