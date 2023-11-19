package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemKeyGold extends Item {
    public ItemKeyGold() {
        super("Gold Key", "A golden key.", Animation.of("items/key/gold_key.png"));
    }
}
