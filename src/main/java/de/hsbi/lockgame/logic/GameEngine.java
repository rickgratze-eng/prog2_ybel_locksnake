package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Snake;
import de.hsbi.lockgame.ui.GamePanel;

import java.util.ArrayList;
import java.util.List;

public final class GameEngine {

    private GameState state;
    private final List<GamePanel> observers = new ArrayList<>();

    public GameEngine(Level level) {
        this.state =
            new GameState(
                level,
                new Snake(List.of(level.snakeStart())),
                level.pins(),
                GameState.Status.RUNNING,
                Direction.NONE);
    }

    public GameState state() {
        return state;
    }

    public void setGamePanel(GamePanel panel) {
        observers.add(panel);
    }

    public void update(Direction d) {
        state =
            new GameState(
                state.level(),
                state.snake(),
                state.pins(),
                state.status(),
                d);

        notifyObservers();
    }

    public void tick() {
        state = state.tick();
        notifyObservers();
    }

    private void notifyObservers() {
        observers.forEach(observer -> observer.update(state));
    }
}
