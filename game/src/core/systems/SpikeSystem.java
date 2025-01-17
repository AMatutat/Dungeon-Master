package core.systems;

import core.System;
import core.components.SpikyComponent;

/**
 * Reduces the current cool down for each {@link SpikyComponent} once per frame.
 *
 * @see SpikyComponent
 */
public class SpikeSystem extends System {

    /** Create new SpikeSystem. */
    public SpikeSystem() {
        super(SpikyComponent.class);
    }

    @Override
    public void execute() {
        entityStream().forEach(e -> e.fetch(SpikyComponent.class).orElseThrow().reduceCoolDown());
    }
}
