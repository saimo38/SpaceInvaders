import java.awt.*;

public abstract class Entity {

    protected int x, y;
    protected int width, height;
    public int hp = 1;

    public abstract void update();
    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
