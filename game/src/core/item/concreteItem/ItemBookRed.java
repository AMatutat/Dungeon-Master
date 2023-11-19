package core.item.concreteItem;

import core.item.Item;
import core.utils.components.draw.Animation;

public class ItemBookRed extends Item {
    public ItemBookRed() {
        super("Red Book", "A red book.", Animation.of("items/book/red_book.png"));
    }
}
