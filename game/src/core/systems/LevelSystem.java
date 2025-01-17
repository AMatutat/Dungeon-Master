package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import core.Entity;
import core.Game;
import core.System;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.generator.graphBased.RoombasedLevelGenerator;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the dungeon game world.
 *
 * <p>The system will store the currently active level.
 *
 * <p>Each frame, this system will draw the level on the screen. The system will also check if one
 * of the entities managed by this system is positioned on the end tile of the level. If so, the
 * next level will be loaded.
 *
 * <p>If a new level is loaded, the system will trigger the onLevelLoad callback given in the
 * constructor of this system.
 *
 * <p>An entity needs a {@link PositionComponent} and a {@link PlayerComponent} to be managed by
 * this system.
 */
public final class LevelSystem extends System {
    /** offset the coordinate by half a tile, it makes every Entity not walk on the sidewalls */
    private static final float X_OFFSET = 0.5f;
    /**
     * offset the coordinate by a quarter tile,it looks a bit more like every Entity is not walking
     * over walls
     */
    private static final float Y_OFFSET = 0.25f;
    /** Currently used level-size configuration for generating new level. */
    private static LevelSize levelSize = LevelSize.MEDIUM;

    private static final String SOUND_EFFECT = "sounds/enterDoor.wav";

    /**
     * The currently loaded level of the game.
     *
     * @see ILevel
     */
    private static ILevel currentLevel;

    private final IVoidFunction onLevelLoad;
    private final Painter painter;

    /**
     * Create a new {@link LevelSize} and register it at the game.
     *
     * <p>The system will not load a new level at generation. The first level will be loaded if this
     * system {@link #execute()} is executed.
     *
     * @param painter The {@link Painter} to use to draw the level.
     * @param onLevelLoad Callback-function that is called if a new level was loaded.
     */
    public LevelSystem(Painter painter, IVoidFunction onLevelLoad) {
        super(PlayerComponent.class, PositionComponent.class);
        this.onLevelLoad = onLevelLoad;
        this.painter = painter;
    }

    /**
     * Get the currently loaded level.
     *
     * @return The currently loaded level.
     */
    public static ILevel level() {
        return currentLevel;
    }

    /**
     * Load a new level with the configured size and random design. *
     *
     * <p>Will trigger the onLevelLoad callback.
     */
    public void loadLevel() {
        loadLevel(RoombasedLevelGenerator.level());
    }

    public void loadLevel(ILevel level) {
        currentLevel = level;
        onLevelLoad.execute();
    }

    private void drawLevel() {
        Map<String, PainterConfig> mapping = new HashMap<>();

        Tile[][] layout = currentLevel.layout();
        for (Tile[] tiles : layout) {
            for (int x = 0; x < layout[0].length; x++) {
                Tile t = tiles[x];
                if (t.levelElement() != LevelElement.SKIP) {
                    String texturePath = t.texturePath();
                    if (!mapping.containsKey(texturePath)) {
                        mapping.put(
                                texturePath, new PainterConfig(texturePath, X_OFFSET, Y_OFFSET));
                    }
                    painter.draw(t.position(), texturePath, mapping.get(texturePath));
                }
            }
        }
    }

    /**
     * Check if the given entity is on the end tile.
     *
     * @param entity The entity for which the position is checked.
     * @return True if the entity is on the end tile, else false.
     */
    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        Tile currentTile = Game.tileAT(pc.position());
        return currentTile.equals(Game.endTile());
    }

    private Optional<ILevel> isOnDoor(Entity entity) {
        ILevel nextLevel = null;
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        for (DoorTile door : currentLevel.doorTiles()) {
            if (door.isOpen()
                    && door.getOtherDoor().isOpen()
                    && door.equals(Game.tileAT(pc.position()))) {
                door.onEntering(entity);
                nextLevel = door.getOtherDoor().level();
            }
        }
        return Optional.ofNullable(nextLevel);
    }

    private void playSound() {
        Sound doorSound = Gdx.audio.newSound(Gdx.files.internal(SOUND_EFFECT));
        long soundId = doorSound.play();
        doorSound.setLooping(soundId, false);
        doorSound.setVolume(soundId, 0.3f);
    }

    /**
     * Execute the system logic.
     *
     * <p>Will load a new level if no level exists or one of the managed entities are on the end
     * tile.
     *
     * <p>Will draw the level.
     */
    @Override
    public void execute() {
        if (currentLevel == null) loadLevel();
        else if (entityStream().anyMatch(this::isOnEndTile)) loadLevel();
        else
            entityStream()
                    .forEach(
                            e ->
                                    isOnDoor(e)
                                            .ifPresent(
                                                    iLevel -> {
                                                        loadLevel(iLevel);
                                                        playSound();
                                                    }));
        drawLevel();
    }

    /** LevelSystem can't be paused. If it is paused, the level will not be shown anymore. */
    @Override
    public void stop() {
        run = true;
    }
}
