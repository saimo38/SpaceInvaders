import java.awt.*;

public class Ufo extends Entity {

    private final int speed;
    private final int scoreValue;

    public Ufo(int x, int y, int speed, int scoreValue) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.scoreValue = scoreValue;
        width = 50;
        height = 20;
        hp = 1;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    @Override
    public void update() {
        x += speed;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(new Color(220, 60, 60));
        g.fillRect(x + 4, y + 10, width - 8, 6);
        g.fillRect(x + 8, y + 6, width - 16, 6);
        g.fillRect(x + 16, y + 2, width - 32, 4);
        g.fillRect(x + 10, y + 14, 6, 4);
        g.fillRect(x + width - 16, y + 14, 6, 4);
    }
}
