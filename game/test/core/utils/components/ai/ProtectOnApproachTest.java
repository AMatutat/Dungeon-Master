package core.utils.components.ai;

import core.Entity;
import core.Game;
import core.components.AIComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.ai.fight.CollideAI;
import core.utils.components.ai.idle.RadiusWalk;
import core.utils.components.ai.transition.ProtectOnApproach;
import core.utils.components.ai.transition.RangeTransition;

import org.junit.Before;

public class ProtectOnApproachTest {
    private Entity entity;
    private AIComponent entityAI;
    private Entity protectedEntity;
    private Entity hero;
    private final Point pointOfProtect = new Point(0, 0);

    @Before
    public void setup() {

        // Protected Entity
        protectedEntity = new Entity();

        // Add AI Component
        AIComponent protectedAI =
                new AIComponent(new CollideAI(0.2f), new RadiusWalk(0, 50), new RangeTransition(2));
        entity.addComponent(protectedAI);

        // Add Position Component
        entity.addComponent(new PositionComponent(pointOfProtect));

        // Protecting Entity
        entity = new Entity();

        // Add AI Component
        entityAI =
                new AIComponent(
                        new CollideAI(0.2f),
                        new RadiusWalk(0, 50),
                        new ProtectOnApproach(2f, protectedEntity));
        entity.addComponent(entityAI);

        // Add Position Component
        entity.addComponent(new PositionComponent(new Point(0f, 0f)));

        // Hero
        hero = Game.hero().orElse(new Entity());
    }
}