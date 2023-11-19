package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemKeyBlue extends Item {
    public ItemKeyBlue() {
        super("Blue Key", "A blue key", Animation.of("items/key/blue_key.png"));
    }
}
