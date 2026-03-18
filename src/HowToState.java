import java.awt.*;

public class HowToState extends BaseState {
    public HowToState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void draw(Graphics g) {
        panel.drawHowTo(g);
    }

    @Override
    public void mouseClick(Point p) {
        panel.handleHowToClick(p);
    }
}
