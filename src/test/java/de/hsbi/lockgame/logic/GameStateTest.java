package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private Level emptyLevel(int width, int height, List<Pin> pins) {
        CellType[][] cells = new CellType[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }

        return new Level(width, height, cells, pins, new Position(1, 1));
    }

    private Level levelWithWallAt(Position wall) {
        CellType[][] cells = new CellType[4][4];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                cells[x][y] = CellType.EMPTY;
            }
        }

        cells[wall.x()][wall.y()] = CellType.WALL;

        return new Level(4, 4, cells, List.of(), new Position(1, 1));
    }

    private GameState state(
        Level level,
        Snake snake,
        List<Pin> pins,
        Direction direction) {

        return new GameState(
            level,
            snake,
            pins,
            GameState.Status.RUNNING,
            direction);
    }

    @Test
    void initialStateKeepsGivenValues() {
        Level level = emptyLevel(4, 4, List.of());
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(), Direction.RIGHT);

        assertSame(level, state.level());
        assertSame(snake, state.snake());
        assertEquals(GameState.Status.RUNNING, state.status());
        assertEquals(Direction.RIGHT, state.pendingDirection());
    }

    @Test
    void tickWithoutDirectionDoesNothing() {
        Level level = emptyLevel(4, 4, List.of());
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(), Direction.NONE);

        GameState next = state.tick();

        assertSame(state, next);
    }

    @Test
    void tickMovesSnakeIntoDirection() {
        Level level = emptyLevel(4, 4, List.of());
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(), Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(2, next.snake().head().x());
        assertEquals(1, next.snake().head().y());
    }

    @Test
    void wallBlocksMovementAndClearsDirection() {
        Level level = levelWithWallAt(new Position(2, 1));
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(), Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(1, next.snake().head().x());
        assertEquals(1, next.snake().head().y());
        assertEquals(Direction.NONE, next.pendingDirection());
    }

    @Test
    void leavingLevelLosesGame() {
        Level level = emptyLevel(2, 2, List.of());
        Snake snake = new Snake(List.of(new Position(0, 0)));

        GameState state = state(level, snake, List.of(), Direction.LEFT);

        GameState next = state.tick();

        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, next.status());
    }

    @Test
    void selfCollisionLosesGame() {
        Level level = emptyLevel(4, 4, List.of());

        Snake snake =
            new Snake(List.of(
                new Position(1, 1),
                new Position(2, 1),
                new Position(2, 2),
                new Position(1, 2)));

        GameState state = state(level, snake, List.of(), Direction.DOWN);

        GameState next = state.tick();

        assertEquals(GameState.Status.LOST_SELF_COLLISION, next.status());
    }

    @Test
    void lowPinCanBeActivatedFromCorrectDirection() {
        Pin pin =
            new Pin(
                new Position(2, 1),
                Pin.State.LOW,
                Direction.RIGHT);

        Level level = emptyLevel(4, 4, List.of(pin));
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(pin), Direction.RIGHT);

        GameState next = state.tick();

        assertTrue(next.pins().get(0).state().isSet());
    }

    @Test
    void gameIsWonWhenAllPinsAreSet() {
        Pin pin =
            new Pin(
                new Position(2, 1),
                Pin.State.LOW,
                Direction.RIGHT);

        Level level = emptyLevel(4, 4, List.of(pin));
        Snake snake = new Snake(List.of(new Position(1, 1)));

        GameState state = state(level, snake, List.of(pin), Direction.RIGHT);

        GameState next = state.tick();

        assertEquals(GameState.Status.WON, next.status());
    }
}
