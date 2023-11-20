package core.utils.components.ai.transition;

import core.Entity;
import core.components.HealthComponent;
import core.utils.components.MissingComponentException;

import java.util.function.Function;

public class SelfDefendTransition implements Function<Entity, Boolean> {
    @Override
    public Boolean apply(final Entity entity) {
        HealthComponent component =
                entity.fetch(HealthComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, HealthComponent.class));
        return component.currentHealthpoints() < component.maximalHealthpoints();
    }
}