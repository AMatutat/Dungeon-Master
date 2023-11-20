package core.item.concreteItem;

import core.Entity;
import core.Game;
import core.components.InventoryComponent;
import core.components.PositionComponent;
import core.entities.MonsterFactory;
import core.item.Item;
import core.utils.components.draw.Animation;

import java.io.IOException;

public class ItemResourceEgg extends Item {

    public ItemResourceEgg() {
        super(
                "Egg",
                "An egg. What was there before? The chicken or the egg?",
                Animation.of("items/resource/egg.png"));
    }

    @Override
    public void use(Entity e) {
        e.fetch(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            component.remove(this);
                            try {
                                Entity monster = MonsterFactory.randomMonster();
                                monster.fetch(PositionComponent.class)
                                        .orElseThrow()
                                        .position(
                                                e.fetch(PositionComponent.class)
                                                        .orElseThrow()
                                                        .position());
                                Game.add(monster);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
    }
}
