import java.awt.*;
import java.awt.event.KeyEvent;

public class PausedState extends BaseState {
    public PausedState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void draw(Graphics g) {
        panel.drawPlaying(g, true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        panel.handlePausedKeyPressed(e);
    }

    @Override
    public void mouseClick(Point p) {
        panel.handlePausedClick(p);
    }
}
