package core.level.generator.graphBased;

import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.*;
import core.utils.IVoidFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This generator will use the {@link GraphGenerator} and {@link RoomGenerator} to generate a
 * room-based level.
 *
 * <p>Use {@link #level(Set, DesignLabel)} to generate a level. For each Entity-Set, there will be
 * one Room in the level with the given entities.
 *
 * <p>Note that a Room-based Level has no {@link core.level.elements.tile.ExitTile}, so by default,
 * no new level will be loaded by the {@link core.systems.LevelSystem}.
 *
 * <p>Therefore, this Generator does not need to implement the {@link
 * core.level.generator.IGenerator} interface. This Generator also cannot be set as the
 * Level-Generator of the {@link core.systems.LevelSystem}.
 *
 * <p>To use a Room-based Level, use the {@link Game#userOnSetup(IVoidFunction)} method to set your
 * own setup method in your Main method. In your Setup-Method, generate a level by using the {@link
 * #level(Set, DesignLabel)} method and use {@link Game#currentLevel(ILevel)} to set the generated
 * level.
 *
 * <p>Now you can get a dot representation of the level graph in the log.
 */
public class RoombasedLevelGenerator {

    private static final Logger LOGGER = Logger.getLogger(RoombasedLevelGenerator.class.getName());

    /**
     * Get a room-based level with a room for each given entity-set.
     *
     * <p>Now you can get a dot representation of the level graph in the log.
     *
     * @param entities Collection of Entity-Sets. For each Entity-Set, one room will be added to the
     *     level, and the entities will be placed in this room.
     * @param designLabel Design of the level.
     * @return The generated level.
     */
    public static ILevel level(Set<Set<Entity>> entities, DesignLabel designLabel) {
        LevelGraph graph = GraphGenerator.generate(entities);
        RoomGenerator roomG = new RoomGenerator();
        LOGGER.info(graph.toDot());
        // generate TileLevel for each Node
        graph.nodes()
                .forEach(
                        node ->
                                node.level(
                                        new TileLevel(
                                                roomG.layout(
                                                        LevelSize.randomSize(), node.neighbours()),
                                                designLabel)));

        for (LevelGraph.Node node : graph.nodes()) {
            ILevel level = node.level();

            // remove trapdoor exit, in rooms we only use doors
            List<Tile> exits = new ArrayList<>(level.exitTiles());
            exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));

            configureDoors(node);
            node.level().onFirstLoad(() -> node.entities().forEach(Game::add));
        }
        return graph.root().level();
    }

    /**
     * Find each door in each room and connect it to the corresponding door in the other room.
     *
     * <p>Will also set the doorstep coordinate, so you will not spawn on the door after you have
     * entered it.
     *
     * @param node Node to configure the doors for.
     */
    private static void configureDoors(LevelGraph.Node node) {
        for (DoorTile door : node.level().doorTiles()) {
            LevelGraph.Direction doorDirection = doorDirection(node, door);

            // find neighbour door
            LevelGraph.Node neighbour = node.neighbours()[doorDirection.value()];
            DoorTile neighbourDoor = null;
            for (DoorTile doorTile : neighbour.level().doorTiles())
                if (LevelGraph.Direction.opposite(doorDirection)
                        == doorDirection(neighbour, doorTile)) {
                    neighbourDoor = doorTile;
                    break;
                }
            door.setOtherDoor(neighbourDoor);

            // place door steps
            Tile doorStep = null;
            switch (doorDirection) {
                case NORTH -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x, door.coordinate().y - 1));
                case EAST -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x - 1, door.coordinate().y));
                case SOUTH -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x, door.coordinate().y + 1));
                case WEST -> doorStep =
                        door.level()
                                .tileAt(
                                        new Coordinate(
                                                door.coordinate().x + 1, door.coordinate().y));
            }
            door.setDoorstep(doorStep);
        }
    }

    /**
     * Get the direction where a door is placed
     *
     * @param node Node that stores the level
     * @param door door-tile where to find the direction for
     * @return the direction of the door
     */
    private static LevelGraph.Direction doorDirection(LevelGraph.Node node, DoorTile door) {
        LevelElement[][] layout = parseToElementLayout(node.level().layout());
        if (TileTextureFactory.isTopWall(door.coordinate(), layout))
            return LevelGraph.Direction.NORTH;
        if (TileTextureFactory.isRightWall(door.coordinate(), layout))
            return LevelGraph.Direction.EAST;
        if (TileTextureFactory.isBottomWall(door.coordinate(), layout))
            return LevelGraph.Direction.SOUTH;
        return LevelGraph.Direction.WEST;
    }

    /**
     * Get the LevelElement[][] for a Tile[][]
     *
     * @param tileLayout tile layout to parse
     * @return the paresed LevelElement layout.
     */
    private static LevelElement[][] parseToElementLayout(Tile[][] tileLayout) {
        int ySize = tileLayout.length;
        int xSize = tileLayout[0].length;
        LevelElement[][] elementLayout = new LevelElement[ySize][xSize];
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                Tile tile = tileLayout[y][x];
                elementLayout[y][x] = tile.levelElement();
            }
        }
        return elementLayout;
    }
}