package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceFlowerRed extends Item {
    public ItemResourceFlowerRed() {
        super("Red Flower", "A red flower.", Animation.of("items/resource/flower_red.png"));
    }
}
