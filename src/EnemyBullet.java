import java.awt.*;

public class EnemyBullet extends Entity {

    public EnemyBullet(int x, int y) {
        this.x = x;
        this.y = y;
        width = 4;
        height = 10;
    }

    @Override
    public void update() {
        y += 5;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, width, height);
    }
}
