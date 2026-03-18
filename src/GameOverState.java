import java.awt.*;

public class GameOverState extends BaseState {
    public GameOverState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void draw(Graphics g) {
        panel.drawGameOver(g);
    }

    @Override
    public void mouseClick(Point p) {
        panel.handleGameOverClick(p);
    }
}
