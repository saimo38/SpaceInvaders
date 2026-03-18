import java.awt.*;

public class MenuState extends BaseState {
    public MenuState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void draw(Graphics g) {
        panel.drawMenu(g);
    }

    @Override
    public void mouseClick(Point p) {
        panel.handleMenuClick(p);
    }
}
