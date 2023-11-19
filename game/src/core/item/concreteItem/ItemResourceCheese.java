package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceCheese extends Item {
    public ItemResourceCheese() {
        super("Cheese", "A piece of cheese.", Animation.of("items/resource/cheese.png"));
    }
}
