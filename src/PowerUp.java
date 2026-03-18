import java.awt.*;

public class PowerUp extends Entity {

    private PowerUpType type;

    public PowerUp(int x, int y, PowerUpType type) {

        this.x = x;
        this.y = y;
        this.type = type;

        width = 15;
        height = 15;
    }

    public PowerUpType getType() {
        return type;
    }

    @Override
    public void update() {
        y += 2;
    }

    @Override
    public void draw(Graphics g) {

        switch (type) {
            case RAPID_FIRE:
                g.setColor(Color.CYAN);
                break;
            case SHIELD:
                g.setColor(Color.BLUE);
                break;
            case CREDITS:
                g.setColor(Color.YELLOW);
                break;
        }

        g.fillOval(x, y, width, height);
    }
}
