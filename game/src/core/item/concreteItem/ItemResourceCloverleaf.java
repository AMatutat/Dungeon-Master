package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceCloverleaf extends Item {
    public ItemResourceCloverleaf() {
        super(
                "Cloverleaf",
                "A cloverleaf. May it bring you luck!",
                Animation.of("items/resource/cloverleaf.png"));
    }
}