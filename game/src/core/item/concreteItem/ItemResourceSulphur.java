package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemResourceSulphur extends Item {
    public ItemResourceSulphur() {
        super("Sulphur", "Some sulphur.", Animation.of("items/resource/sulphur.png"));
    }
}
