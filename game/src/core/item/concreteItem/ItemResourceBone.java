package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceBone extends Item {
    public ItemResourceBone() {
        super("Bone", "A bone. Who knows what it's from?", Animation.of("items/resource/bone.png"));
    }
}