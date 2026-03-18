import java.awt.*;

public class ShopState extends BaseState {
    public ShopState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void draw(Graphics g) {
        panel.drawShop(g);
    }

    @Override
    public void mouseClick(Point p) {
        panel.handleShopClick(p);
    }
}
