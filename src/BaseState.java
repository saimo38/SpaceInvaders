public abstract class BaseState implements StateHandler {
    protected final GamePanel panel;

    protected BaseState(GamePanel panel) {
        this.panel = panel;
    }
}
