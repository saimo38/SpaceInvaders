import java.awt.*;
import java.awt.event.KeyEvent;

public class PlayingState extends BaseState {
    public PlayingState(GamePanel panel) {
        super(panel);
    }

    @Override
    public void update() {
        panel.updateGame();
    }

    @Override
    public void draw(Graphics g) {
        panel.drawPlaying(g, false);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        panel.handlePlayingKeyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        panel.handlePlayingKeyReleased(e);
    }
}
