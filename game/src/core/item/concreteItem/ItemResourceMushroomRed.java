package core.item.concreteItem;

import core.Entity;
import core.components.HealthComponent;
import core.components.InventoryComponent;
import core.item.Item;
import core.utils.components.draw.Animation;
import core.utils.components.health.Damage;
import core.utils.components.health.DamageType;

public class ItemResourceMushroomRed extends Item {

    private static final int DAMAGE_AMOUNT = 20;

    public ItemResourceMushroomRed() {
        super("Red Mushroom", "A red mushroom.", Animation.of("items/resource/mushroom_red.png"));
    }

    @Override
    public void use(Entity e) {
        e.fetch(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            component.remove(this);
                            e.fetch(HealthComponent.class)
                                    .ifPresent(
                                            hc ->
                                                    hc.receiveHit(
                                                            new Damage(
                                                                    DAMAGE_AMOUNT,
                                                                    DamageType.POISON,
                                                                    null)));
                        });
    }
}
