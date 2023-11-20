package core.utils.components;

import com.badlogic.gdx.Gdx;

import core.Entity;
import core.Game;
import core.components.AIComponent;
import core.components.CollideComponent;
import core.components.DrawComponent;
import core.components.HealthComponent;
import core.components.PositionComponent;
import core.components.UIComponent;
import core.components.VelocityComponent;
import core.configuration.KeyboardConfig;
import core.hud.UITools;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.systems.CameraSystem;
import core.utils.IVoidFunction;
import core.utils.Point;
import core.utils.components.ai.fight.CollideAI;
import core.utils.components.ai.idle.RadiusWalk;
import core.utils.components.ai.transition.SelfDefendTransition;
import core.utils.components.skill.SkillTools;
import core.utils.logging.CustomLogLevel;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Auxiliary class to accelerate the creation and testing of specific game scenarios.
 *
 * <p>It provides useful functionalities that can aid in verifying the correct behavior of a game
 * implementation.
 *
 * <p>Add the Debugger in the Game-Loop by adding the {@link #execute()} call in {@link
 * Game#userOnFrame(IVoidFunction)}
 *
 * @see KeyboardConfig
 */
public class Debugger {

    private static final Logger LOGGER = Logger.getLogger(Debugger.class.getName());

    /**
     * Zooms the camera in or out by the given amount.
     *
     * @param amount the length of the zoom change
     */
    public static void ZOOM_CAMERA(float amount) {
        LOGGER.log(CustomLogLevel.DEBUG, "Change Camera Zoom " + amount);
        CameraSystem.camera().zoom = Math.max(0.1f, CameraSystem.camera().zoom + amount);
        LOGGER.log(CustomLogLevel.DEBUG, "Camera Zoom is now " + CameraSystem.camera().zoom);
    }

    /** Teleports the Hero to the current position of the cursor. */
    public static void TELEPORT_TO_CURSOR() {
        LOGGER.log(CustomLogLevel.DEBUG, "TELEPORT TO CURSOR");
        TELEPORT(SkillTools.cursorPositionAsPoint());
    }

    /** Teleports the Hero to the end of the level, on a neighboring accessible tile if possible. */
    public static void TELEPORT_TO_END() {
        LOGGER.info("TELEPORT TO END");
        Coordinate endTile = Game.endTile().coordinate();
        Coordinate[] neighborTiles = {
            new Coordinate(endTile.x + 1, endTile.y),
            new Coordinate(endTile.x - 1, endTile.y),
            new Coordinate(endTile.x, endTile.y + 1),
            new Coordinate(endTile.x, endTile.y - 1)
        };
        for (Coordinate neighborTile : neighborTiles) {
            Tile neighbor = Game.tileAT(neighborTile);
            if (neighbor.isAccessible()) {
                TELEPORT(neighbor);
                return;
            }
        }
    }

    /** Will teleport the Hero on the EndTile so the next level gets loaded */
    public static void LOAD_NEXT_LEVEL() {
        LOGGER.info("TELEPORT ON END");
        TELEPORT(Game.endTile());
    }

    /** Teleports the hero to the start of the level. */
    public static void TELEPORT_TO_START() {
        LOGGER.info("TELEPORT TO START");
        TELEPORT(Game.startTile());
    }

    /**
     * Teleports the hero to the given tile.
     *
     * @param targetLocation the tile to teleport to
     */
    public static void TELEPORT(Tile targetLocation) {
        TELEPORT(targetLocation.position());
    }

    /**
     * Teleports the hero to the given location.
     *
     * @param targetLocation the location to teleport to
     */
    public static void TELEPORT(Point targetLocation) {
        if (Game.hero().isPresent()) {
            PositionComponent pc =
                    Game.hero()
                            .get()
                            .fetch(PositionComponent.class)
                            .orElseThrow(
                                    () ->
                                            MissingComponentException.build(
                                                    Game.hero().get(), PositionComponent.class));

            // Attempt to teleport to targetLocation
            LOGGER.log(
                    CustomLogLevel.DEBUG,
                    "Trying to teleport to " + targetLocation.x + ":" + targetLocation.y);
            Tile t = Game.tileAT(targetLocation);
            if (t == null || !t.isAccessible()) {
                LOGGER.info("Cannot teleport to non-existing or non-accessible tile");
                return;
            }

            pc.position(targetLocation);
            LOGGER.info("Teleport successful");
        }
    }

    /** Spawns a monster at the cursor's position. */
    public static void SPAWN_MONSTER_ON_CURSOR() {
        LOGGER.info("Spawn Monster on Cursor");
        SPAWN_MONSTER(SkillTools.cursorPositionAsPoint());
    }

    /**
     * Spawn a monster at the given position if it is in the level and accessible.
     *
     * @param position The location to spawn the monster on.
     */
    public static void SPAWN_MONSTER(Point position) {
        // Get the tile at the given position
        Tile tile = null;
        try {
            tile = Game.tileAT(position);
        } catch (NullPointerException ex) {
            LOGGER.info(ex.getMessage());
        }

        // If the tile is accessible, spawn a monster at the position
        if (tile != null && tile.isAccessible()) {
            Entity monster = new Entity("Debug Monster");

            // Add components to the monster entity
            monster.addComponent(new PositionComponent(position));
            try {
                monster.addComponent(new DrawComponent("character/monster/chort"));
            } catch (IOException e) {
                LOGGER.warning(
                        "The DrawComponent for the chort cant be created. " + e.getMessage());
            }
            monster.addComponent(new VelocityComponent(0.1f, 0.1f));
            monster.addComponent(new HealthComponent());
            monster.addComponent(new CollideComponent());
            monster.addComponent(
                    new AIComponent(
                            new CollideAI(1), new RadiusWalk(5, 1), new SelfDefendTransition()));

            Game.add(monster);
            // Log that the monster was spawned
            LOGGER.info("Spawned monster at position " + position);
        } else {
            // Log that the monster couldn't be spawned
            LOGGER.info("Cannot spawn monster at non-existent or non-accessible tile");
        }
    }

    private static Entity pauseMenu;

    public static void PAUSE_GAME() {
        if (pauseMenu == null
                || pauseMenu
                        .fetch(UIComponent.class)
                        .map(x -> x.dialog().getStage() == null)
                        .orElse(true)) pauseMenu = newPauseMenu();
    }

    private static Entity newPauseMenu() {
        Entity entity = UITools.generateNewTextDialog("Pause", "Weiter", "Pausemenu");
        entity.fetch(UIComponent.class).ifPresent(y -> y.dialog().setVisible(true));
        return entity;
    }

    /**
     * Checks for key input corresponding to Debugger functionalities, and executes the relevant
     * function if detected.
     */
    public void execute() {
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_OUT.value()))
            Debugger.ZOOM_CAMERA(-0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_ZOOM_IN.value()))
            Debugger.ZOOM_CAMERA(0.2f);
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_CURSOR.value()))
            Debugger.TELEPORT_TO_CURSOR();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_END.value()))
            Debugger.TELEPORT_TO_END();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_TO_START.value()))
            Debugger.TELEPORT_TO_START();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_TELEPORT_ON_END.value()))
            Debugger.LOAD_NEXT_LEVEL();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.DEBUG_SPAWN_MONSTER.value()))
            Debugger.SPAWN_MONSTER_ON_CURSOR();
        if (Gdx.input.isKeyJustPressed(KeyboardConfig.PAUSE.value())) Debugger.PAUSE_GAME();
    }
}