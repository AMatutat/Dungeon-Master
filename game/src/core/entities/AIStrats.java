package core.entities;

import core.Entity;
import core.components.AIComponent;
import core.utils.components.ai.fight.CollideAI;
import core.utils.components.ai.fight.RangeAI;
import core.utils.components.ai.idle.PatrolWalk;
import core.utils.components.ai.idle.RadiusWalk;
import core.utils.components.ai.transition.RangeTransition;
import core.utils.components.ai.transition.SelfDefendTransition;
import core.utils.components.skill.FireballSkill;
import core.utils.components.skill.Skill;
import core.utils.components.skill.SkillTools;

import java.util.function.Consumer;
import java.util.function.Function;

public class AIStrats {

    private static Consumer<Entity> f;

    public static AIComponent setupAI(
            MonsterFactory.AIStrat strat, MonsterFactory.Strength strength) {
        Consumer<Entity> fight;
        Consumer<Entity> idle;
        Function<Entity, Boolean> trans;
        switch (strat.idle) {
            case "PATROL" -> idle = patrolWalk(strength);
            default -> idle = radiusWalk(strength);
        }
        switch (strat.fight) {
            case "RANGE" -> fight = rangeAI(strength);
            default -> fight = collide(strength);
        }

        switch (strat.trans) {
            case "RANGE" -> trans = rangeTransition(strength);
            default -> trans = selfDefendTransition(strength);
        }
        return new AIComponent(fight, idle, trans);
    }

    private static Function<Entity, Boolean> rangeTransition(MonsterFactory.Strength strength) {
        float range;
        switch (strength) {
            case EASY -> range = 3f;
            case MEDIUM -> range = 5f;
            default -> range = 8f;
        }
        return new RangeTransition(range);
    }

    private static Consumer<Entity> collide(MonsterFactory.Strength strength) {
        float range;
        switch (strength) {
            case EASY -> range = 2f;
            case MEDIUM -> range = 3f;
            default -> range = 5f;
        }
        return new CollideAI(range);
    }

    private static Consumer<Entity> radiusWalk(MonsterFactory.Strength strength) {
        float range;
        switch (strength) {
            case EASY -> range = 5f;
            case MEDIUM -> range = 9f;
            default -> range = 12f;
        }
        return new RadiusWalk(range, 2);
    }

    private static Consumer<Entity> patrolWalk(MonsterFactory.Strength strength) {
        float range;
        switch (strength) {
            case EASY -> range = 5f;
            case MEDIUM -> range = 9f;
            default -> range = 12f;
        }
        return new PatrolWalk(range, 3, 2, PatrolWalk.MODE.RANDOM);
    }

    private static Consumer<Entity> rangeAI(MonsterFactory.Strength strength) {
        float range;
        switch (strength) {
            case EASY -> range = 4f;
            case MEDIUM -> range = 5f;
            default -> range = 6f;
        }
        return new RangeAI(
                range,
                0f,
                new Skill(
                        new FireballSkill(
                                SkillTools::heroPositionAsPoint,
                                strength.skillSpeed,
                                strength.skillDamage),
                        strength.skillCooldown));
    }

    private static Function<Entity, Boolean> selfDefendTransition(
            MonsterFactory.Strength strength) {
        return new SelfDefendTransition();
    }
}
