package core.item.concreteItem;

import core.Entity;
import core.components.HealthComponent;
import core.components.InventoryComponent;
import core.item.Item;
import core.utils.components.draw.Animation;
import core.utils.components.health.Damage;
import core.utils.components.health.DamageType;

public class ItemPotionHealth extends Item {

    private static final int HEAL_AMOUNT = 50;

    public ItemPotionHealth() {
        super(
                "Health Potion",
                "A health potion. It heals you for " + HEAL_AMOUNT + " health points.",
                Animation.of("items/potion/health_potion.png"));
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
                                                                    -HEAL_AMOUNT,
                                                                    DamageType.HEAL,
                                                                    null)));
                        });
    }
}
