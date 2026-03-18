import java.awt.*;

public class Bullet extends Entity {

    private final int speed;

    public Bullet(int x, int y) {
        this(x, y, 8);
    }

    public Bullet(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        width = 4;
        height = 10;
        this.speed = speed;
    }

    @Override
    public void update() {
        y -= speed;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, width, height);
    }
}
