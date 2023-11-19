package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceTentacle extends Item {
    public ItemResourceTentacle() {
        super("Tentacle", "A tentacle", Animation.of("items/resource/tentacle.png"));
    }
}
