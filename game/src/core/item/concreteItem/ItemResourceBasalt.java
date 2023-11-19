package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceBasalt extends Item {
    public ItemResourceBasalt() {
        super("Basalt", "Its just basalt.", Animation.of("items/resource/basalt.png"));
    }
}
