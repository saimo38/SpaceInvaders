import java.awt.*;
import java.awt.event.KeyEvent;

public interface StateHandler {
    default void update() {}
    default void draw(Graphics g) {}
    default void keyPressed(KeyEvent e) {}
    default void keyReleased(KeyEvent e) {}
    default void mouseClick(Point p) {}
}
