package core.level.generator.graphBased;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.entities.EntityFactory;
import core.entities.MonsterFactory;
import core.level.Tile;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.elements.tile.DoorTile;
import core.level.generator.GeneratorUtils;
import core.level.generator.graphBased.levelGraph.Direction;
import core.level.generator.graphBased.levelGraph.LevelGraph;
import core.level.generator.graphBased.levelGraph.LevelNode;
import core.level.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class RoombasedLevelGenerator {

    /** Rooms with this amount or fewer entities will be generated small. */
    private static final int MAX_ENTITIES_FOR_SMALL_ROOMS = 2;
    /** Rooms with this amount or more entities will be generated large. */
    private static final int MIN_ENTITIES_FOR_BIG_ROOM = 5;

    private static final Logger LOGGER = Logger.getLogger(RoombasedLevelGenerator.class.getName());

    public static ILevel level() {
        Set<Set<Entity>> entities = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Set<Entity> set = new HashSet<>();
            entities.add(set);
            if (i == 10 / 2) {
                try {
                    set.add(EntityFactory.newCraftingCauldron());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int j = 0; j < 5; j++) {
                try {
                    if (j < 3) set.add(MonsterFactory.randomMonster(MonsterFactory.Strength.EASY));
                    else if (i == 3)
                        set.add(MonsterFactory.randomMonster(MonsterFactory.Strength.MEDIUM));
                    else set.add(MonsterFactory.randomMonster(MonsterFactory.Strength.HARD));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int k = 0; k < 1; k++) {
                try {
                    set.add(EntityFactory.newChest());
                } catch (IOException e) {

                }
            }
        }
        return level(entities, DesignLabel.randomDesign());
    }

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
        return level(LevelGraphGenerator.generate(entities), designLabel);
    }

    /**
     * Get a room-based level with a given level graph.
     *
     * <p>Now you can get a dot representation of the level graph in the log.
     *
     * @param graph level graph to generate the level for.
     * @param designLabel Design of the level.
     * @return The generated level.
     */
    public static ILevel level(final LevelGraph graph, DesignLabel designLabel) {
        RoomGenerator roomG = new RoomGenerator();
        LOGGER.info(graph.toDot());
        // generate TileLevel for each Node
        graph.nodes()
                .forEach(
                        node ->
                                node.level(
                                        new TileLevel(
                                                roomG.layout(sizeFor(node), node.neighbours()),
                                                designLabel)));

        List<LevelNode> removeExitFrom = new ArrayList<>(graph.nodes());
        Random random = new Random();
        int randomIndex = random.nextInt(removeExitFrom.size());
        removeExitFrom.remove(randomIndex);
        for (LevelNode node : removeExitFrom) {
            ILevel level = node.level();
            // remove trapdoor exit, in rooms we only use doors
            List<Tile> exits = new ArrayList<>(level.exitTiles());
            exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));
        }

        // removeExitFrom.remove(randomIndex);
        for (LevelNode node : graph.nodes()) {
            ILevel level = node.level();
            configureDoors(node);
            node.level().onFirstLoad(() -> node.entities().forEach(Game::add));
        }
        return graph.root().level();
    }

    private static LevelSize sizeFor(LevelNode node) {
        AtomicInteger count = new AtomicInteger();
        node.entities()
                .forEach(
                        e -> {
                            if (e.isPresent(PositionComponent.class)
                                    && e.isPresent(DrawComponent.class)) count.getAndIncrement();
                        });

        if (count.get() <= MAX_ENTITIES_FOR_SMALL_ROOMS) return LevelSize.SMALL;
        else if (count.get() >= MIN_ENTITIES_FOR_BIG_ROOM) return LevelSize.LARGE;
        else return LevelSize.MEDIUM;
    }

    /**
     * Find each door in each room and connect it to the corresponding door in the other room.
     *
     * <p>Will also set the doorstep coordinate, so you will not spawn on the door after you have
     * entered it.
     *
     * @param node Node to configure the doors for.
     */
    private static void configureDoors(LevelNode node) {
        for (DoorTile door : node.level().doorTiles()) {
            Direction doorDirection = GeneratorUtils.doorDirection(node.level(), door);

            // find neighbour door
            LevelNode neighbour = node.neighbours()[doorDirection.value()];
            DoorTile neighbourDoor = null;
            for (DoorTile doorTile : neighbour.level().doorTiles())
                if (Direction.opposite(doorDirection)
                        == GeneratorUtils.doorDirection(neighbour.level(), doorTile)) {
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
}
