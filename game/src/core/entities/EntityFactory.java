package core.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import core.Entity;
import core.Game;
import core.components.*;
import core.configuration.KeyboardConfig;
import core.hud.GUICombination;
import core.hud.crafting.CraftingGUI;
import core.hud.inventory.InventoryGUI;
import core.item.Item;
import core.level.Tile;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.ItemDataGenerator;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.ChestAnimations;
import core.utils.components.draw.CoreAnimations;
import core.utils.components.health.Damage;
import core.utils.components.interaction.DropItemsInteraction;
import core.utils.components.interaction.InteractionTool;
import core.utils.components.skill.FireballSkill;
import core.utils.components.skill.Skill;
import core.utils.components.skill.SkillTools;

import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {
    public static final int DEFAULT_INVENTORY_SIZE = 10;
    private static final Random RANDOM = new Random();
    private static final String HERO_FILE_PATH = "character/wizard";
    private static final float X_SPEED_HERO = 7.5f;
    private static final float Y_SPEED_HERO = 7.5f;
    private static final int FIREBALL_COOL_DOWN = 500;
    private static final int HERO_HP = 100;

    /**
     * Create a new Entity that can be used as a playable character. It will have a {@link
     * CameraComponent}, {@link PlayerComponent}. {@link PositionComponent}, {@link
     * VelocityComponent} {@link DrawComponent}, {@link CollideComponent}, {@link HealthComponent}
     * and {@link XPComponent}.
     *
     * @return Created Entity
     */
    public static Entity newHero() throws IOException {
        Entity hero = new Entity("hero");
        CameraComponent cc = new CameraComponent();
        hero.addComponent(cc);
        PositionComponent poc = new PositionComponent();
        hero.addComponent(poc);
        hero.addComponent(new VelocityComponent(X_SPEED_HERO, Y_SPEED_HERO));
        hero.addComponent(new DrawComponent(HERO_FILE_PATH));
        HealthComponent hc =
                new HealthComponent(
                        HERO_HP,
                        entity -> {
                            // play sound
                            Sound sound =
                                    Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
                            long soundId = sound.play();
                            sound.setLooping(soundId, false);
                            sound.setVolume(soundId, 0.3f);
                            sound.setLooping(soundId, false);
                            sound.play();
                            sound.setVolume(soundId, 0.9f);

                            // relink components for camera
                            Entity cameraDummy = new Entity();
                            cameraDummy.addComponent(cc);
                            cameraDummy.addComponent(poc);
                            Game.hero(null);
                            try {
                                cameraDummy.addComponent(new DrawComponent("objects/skull"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            Game.add(cameraDummy);
                        });
        hero.addComponent(hc);
        hero.addComponent(
                new CollideComponent(
                        (you, other, direction) ->
                                other.fetch(SpikyComponent.class)
                                        .ifPresent(
                                                spikyComponent -> {
                                                    if (spikyComponent.isActive()) {
                                                        hc.receiveHit(
                                                                new Damage(
                                                                        spikyComponent
                                                                                .damageAmount(),
                                                                        spikyComponent.damageType(),
                                                                        other));
                                                        spikyComponent.activateCoolDown();
                                                    }
                                                }),
                        (you, other, direction) -> {}));

        hero.addComponent(new XPComponent((e) -> {}));
        PlayerComponent pc = new PlayerComponent();
        hero.addComponent(pc);
        InventoryComponent ic = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
        hero.addComponent(ic);
        Skill fireball =
                new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

        // hero movement
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_UP.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));
                    vc.currentYVelocity(1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_DOWN.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentYVelocity(-1 * vc.yVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_RIGHT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(1 * vc.xVelocity());
                });
        pc.registerCallback(
                KeyboardConfig.MOVEMENT_LEFT.value(),
                entity -> {
                    VelocityComponent vc =
                            entity.fetch(VelocityComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    MissingComponentException.build(
                                                            entity, VelocityComponent.class));

                    vc.currentXVelocity(-1 * vc.xVelocity());
                });

        pc.registerCallback(
                KeyboardConfig.INVENTORY_OPEN.value(),
                (e) -> {
                    UIComponent uiComponent = e.fetch(UIComponent.class).orElse(null);
                    if (uiComponent != null) {
                        if (uiComponent.dialog() instanceof GUICombination) {
                            InventoryGUI.inHeroInventory = false;
                            e.removeComponent(UIComponent.class);
                        }
                    } else {
                        InventoryGUI.inHeroInventory = true;
                        e.addComponent(
                                new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
                    }
                },
                false,
                false);

        pc.registerCallback(
                KeyboardConfig.CLOSE_UI.value(),
                (e) -> {
                    var firstUI =
                            Game.entityStream() // would be nice to directly access HudSystems
                                    // stream (no access to the System object)
                                    .filter(
                                            x ->
                                                    x.isPresent(
                                                            UIComponent.class)) // find all Entities
                                    // which have a
                                    // UIComponent
                                    .map(
                                            x ->
                                                    new Tuple<>(
                                                            x,
                                                            x.fetch(UIComponent.class)
                                                                    .orElseThrow(
                                                                            () ->
                                                                                    MissingComponentException
                                                                                            .build(
                                                                                                    x,
                                                                                                    UIComponent
                                                                                                            .class)))) // create a tuple to
                                    // still have access to
                                    // the UI Entity
                                    .filter(x -> x.b().closeOnUICloseKey())
                                    .max(
                                            Comparator.comparingInt(
                                                    x -> x.b().dialog().getZIndex())) // find dialog
                                    // with highest
                                    // z-Index
                                    .orElse(null);
                    if (firstUI != null) {
                        InventoryGUI.inHeroInventory = false;
                        firstUI.a().removeComponent(UIComponent.class);
                        if (firstUI.a().componentStream().findAny().isEmpty()) {
                            Game.remove(firstUI.a()); // delete unused Entity
                        }
                    }
                },
                false,
                false);

        pc.registerCallback(
                KeyboardConfig.INTERACT_WORLD.value(),
                InteractionTool::interactWithClosestInteractable,
                false);

        pc.registerCallback(
                KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
                hero1 -> {
                    // only interact with entities the cursor points at
                    Point mousePosition = SkillTools.cursorPositionAsPoint();
                    Tile mouseTile = Game.tileAT(mousePosition);
                    if (mouseTile == null) return; // mouse out of bound

                    Game.entityAtTile(mouseTile)
                            .filter(e -> e.isPresent(InteractionComponent.class))
                            .findFirst()
                            .ifPresent(
                                    interactable -> {
                                        InteractionComponent ic1 =
                                                interactable
                                                        .fetch(InteractionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        interactable,
                                                                                        InteractionComponent
                                                                                                .class));
                                        PositionComponent pc1 =
                                                interactable
                                                        .fetch(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        interactable,
                                                                                        PositionComponent
                                                                                                .class));
                                        PositionComponent heroPC =
                                                hero1.fetch(PositionComponent.class)
                                                        .orElseThrow(
                                                                () ->
                                                                        MissingComponentException
                                                                                .build(
                                                                                        hero1,
                                                                                        PositionComponent
                                                                                                .class));
                                        if (Point.calculateDistance(
                                                        pc1.position(), heroPC.position())
                                                < ic1.radius())
                                            ic1.triggerInteraction(interactable, hero1);
                                    });
                },
                false);

        // skills
        pc.registerCallback(KeyboardConfig.FIRST_SKILL.value(), fireball::execute);

        return hero;
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * <p>{@link ItemDataGenerator} is used to generate random items
     *
     * @return Created Entity
     */
    public static Entity newChest() throws IOException {
        ItemDataGenerator itemDataGenerator = new ItemDataGenerator();

        Set<Item> items =
                IntStream.range(0, RANDOM.nextInt(1, 3))
                        .mapToObj(i -> itemDataGenerator.generateItemData())
                        .collect(Collectors.toSet());
        return newChest(items, PositionComponent.ILLEGAL_POSITION);
    }

    /**
     * Create a new Entity that can be used as a chest.
     *
     * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
     * DrawComponent}, {@link CollideComponent} and {@link InventoryComponent}. It will use the
     * {@link DropItemsInteraction} on interaction.
     *
     * @param item The {@link Item} for the Items inside the chest.
     * @param position The position of the chest.
     * @return Created Entity
     */
    public static Entity newChest(Set<Item> item, Point position) throws IOException {
        final float defaultInteractionRadius = 1f;
        Entity chest = new Entity("chest");

        if (position == null) chest.addComponent(new PositionComponent());
        else chest.addComponent(new PositionComponent(position));
        InventoryComponent ic = new InventoryComponent(item.size());
        chest.addComponent(ic);
        item.forEach(ic::add);
        chest.addComponent(
                new InteractionComponent(
                        defaultInteractionRadius,
                        true,
                        (interacted, interactor) -> {
                            interactor
                                    .fetch(InventoryComponent.class)
                                    .ifPresent(
                                            whoIc -> {
                                                UIComponent uiComponent =
                                                        new UIComponent(
                                                                new GUICombination(
                                                                        new InventoryGUI(whoIc),
                                                                        new InventoryGUI(ic)),
                                                                true);
                                                uiComponent.onClose(
                                                        () ->
                                                                interacted
                                                                        .fetch(DrawComponent.class)
                                                                        .ifPresent(
                                                                                interactedDC -> {
                                                                                    // remove all
                                                                                    // prior
                                                                                    // opened
                                                                                    // animations
                                                                                    interactedDC
                                                                                            .deQueueByPriority(
                                                                                                    ChestAnimations
                                                                                                            .OPEN_FULL
                                                                                                            .priority());
                                                                                    if (ic.count()
                                                                                            > 0) {
                                                                                        // as long
                                                                                        // as
                                                                                        // there is
                                                                                        // an
                                                                                        // item
                                                                                        // inside
                                                                                        // the chest
                                                                                        // show a
                                                                                        // full
                                                                                        // chest
                                                                                        interactedDC
                                                                                                .queueAnimation(
                                                                                                        ChestAnimations
                                                                                                                .OPEN_FULL);
                                                                                    } else {
                                                                                        // empty
                                                                                        // chest
                                                                                        // show the
                                                                                        // empty
                                                                                        // animation
                                                                                        interactedDC
                                                                                                .queueAnimation(
                                                                                                        ChestAnimations
                                                                                                                .OPEN_EMPTY);
                                                                                    }
                                                                                }));
                                                interactor.addComponent(uiComponent);
                                            });
                            interacted
                                    .fetch(DrawComponent.class)
                                    .ifPresent(
                                            interactedDC -> {
                                                // only add opening animation when it is not
                                                // finished
                                                if (interactedDC
                                                        .getAnimation(ChestAnimations.OPENING)
                                                        .map(animation -> !animation.isFinished())
                                                        .orElse(true)) {
                                                    interactedDC.queueAnimation(
                                                            ChestAnimations.OPENING);
                                                }
                                            });
                        }));
        DrawComponent dc = new DrawComponent("objects/treasurechest");
        var mapping = dc.animationMap();
        // set the closed chest as default idle
        mapping.put(
                CoreAnimations.IDLE.pathString(), mapping.get(ChestAnimations.CLOSED.pathString()));
        // opening animation should not loop
        mapping.get(ChestAnimations.OPENING.pathString()).setLoop(false);
        // reset Idle Animation
        dc.deQueueByPriority(CoreAnimations.IDLE.priority());
        dc.currentAnimation(CoreAnimations.IDLE);
        chest.addComponent(dc);

        return chest;
    }

    /**
     * Create a new Entity that can be used as a crafting cauldron.
     *
     * @return Created Entity
     * @throws IOException if the textures do not exist
     */
    public static Entity newCraftingCauldron() throws IOException {
        Entity cauldron = new Entity("cauldron");
        cauldron.addComponent(new PositionComponent());
        cauldron.addComponent(new DrawComponent("objects/cauldron"));
        cauldron.addComponent(new CollideComponent());
        cauldron.addComponent(
                new InteractionComponent(
                        1f,
                        true,
                        (entity, who) ->
                                who.fetch(InventoryComponent.class)
                                        .ifPresent(
                                                ic -> {
                                                    CraftingGUI craftingGUI = new CraftingGUI(ic);
                                                    UIComponent component =
                                                            new UIComponent(
                                                                    new GUICombination(
                                                                            new InventoryGUI(ic),
                                                                            craftingGUI),
                                                                    true);
                                                    component.onClose(craftingGUI::cancel);
                                                    who.addComponent(component);
                                                })));
        return cauldron;
    }
}
