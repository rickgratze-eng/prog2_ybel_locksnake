package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import java.util.ArrayList;
import java.util.List;

public final class GameState {
    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(
        Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        this.level = level;
        this.snake = snake;
        this.pins = List.copyOf(pins);
        this.status = status;
        this.pendingDirection = pendingDirection;
    }

    public Level level() {
        return level;
    }

    public Snake snake() {
        return snake;
    }

    public List<Pin> pins() {
        return pins;
    }

    public Status status() {
        return status;
    }

    public Direction pendingDirection() {
        return pendingDirection;
    }

    public GameState tick() {
        if (!status.isRunning() || pendingDirection == Direction.NONE) {
            return this;
        }

        Position next = snake.nextHead(pendingDirection);

        if (!level.isInside(next)) {
            return new GameState(level, snake, pins, Status.LOST_OUT_OF_BOUNDS, Direction.NONE);
        }

        if (level.cellAt(next) == CellType.WALL) {
            return new GameState(level, snake, pins, status, Direction.NONE);
        }

        if (snakeOccupies(next)) {
            return new GameState(level, snake, pins, Status.LOST_SELF_COLLISION, Direction.NONE);
        }

        Pin pin = pinAt(next);
        if (pin != null) {
            if (!pin.state().isSet() && pin.activationDirection() == pendingDirection) {
                List<Pin> newPins = activatePin(pin);
                Status newStatus = allPinsSet(newPins) ? Status.WON : Status.RUNNING;
                return new GameState(level, snake, newPins, newStatus, Direction.NONE);
            }

            return new GameState(level, snake, pins, status, Direction.NONE);
        }

        Snake newSnake = snake.grow(pendingDirection);
        return new GameState(level, newSnake, pins, status, pendingDirection);
    }

    private boolean snakeOccupies(Position position) {
        return snake.body().stream().anyMatch(p -> samePosition(p, position));
    }

    private Pin pinAt(Position position) {
        return pins.stream()
            .filter(pin -> samePosition(pin.position(), position))
            .findFirst()
            .orElse(null);
    }

    private List<Pin> activatePin(Pin pinToActivate) {
        List<Pin> newPins = new ArrayList<>();

        for (Pin pin : pins) {
            if (samePosition(pin.position(), pinToActivate.position())) {
                newPins.add(pin.withState(Pin.State.HIGH));
            } else {
                newPins.add(pin);
            }
        }

        return newPins;
    }

    private boolean allPinsSet(List<Pin> pinsToCheck) {
        return pinsToCheck.stream().allMatch(pin -> pin.state().isSet());
    }

    private boolean samePosition(Position a, Position b) {
        return a.x() == b.x() && a.y() == b.y();
    }

    public enum Status {
        RUNNING,
        WON,
        LOST_SELF_COLLISION,
        LOST_OUT_OF_BOUNDS;

        public boolean isRunning() {
            return this == RUNNING;
        }
    }
}
