package core.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import core.Entity;
import core.Game;
import core.components.*;
import core.item.Item;
import core.utils.components.ItemDataGenerator;
import core.utils.components.health.DamageType;
import core.utils.components.interaction.DropItemsInteraction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class MonsterFactory {

    private static final String BASE_PATH = "character/monster/";
    private static final Random RANDOM = new Random();
    public static final int MONSTER_COLLIDE_COOL_DOWN = 2 * Game.frameRate();

    public static Entity randomMonster() throws IOException {
        return randomMonster(MonsterType.getRandomMonster());
    }

    public static Entity randomMonster(Strength strength) throws IOException {
        return randomMonster(MonsterType.getRandomMonsterByStrength(strength));
    }

    public static Entity randomMonster(MonsterType monsterType) throws IOException {
        Entity monster = new Entity(monsterType.toString());
        BiConsumer<Entity, Entity> onDeath = setupLoot(monster, monsterType.strength);
        monster.addComponent(
                new HealthComponent(monsterType.strength.hp, (e) -> onDeath.accept(e, null)));
        monster.addComponent(new PositionComponent());
        monster.addComponent(AIStrats.setupAI(monsterType.ai, monsterType.strength));
        monster.addComponent(new DrawComponent(BASE_PATH + monsterType.textureName));
        monster.addComponent(
                new VelocityComponent(monsterType.strength.speed, monsterType.strength.speed));
        monster.addComponent(new CollideComponent());

        long xp;
        switch (monsterType.strength) {
            case MEDIUM -> xp = 2;
            case HARD -> xp = 3;
            default -> xp = 1;
        }
        monster.addComponent(new XPComponent(xp));

        monster.addComponent(
                new SpikyComponent(
                        monsterType.strength.collideDamage,
                        monsterType.collideDamageType,
                        MONSTER_COLLIDE_COOL_DOWN));
        monster.addComponent(new IdleSoundComponent(randomMonsterIdleSound()));
        return monster;
    }

    private static BiConsumer<Entity, Entity> setupLoot(Entity monster, Strength strength) {
        int baseBound = 10;
        switch (strength) {
            case MEDIUM -> baseBound--;
            case HARD -> baseBound -= 2;
            default -> baseBound += 0;
        }
        // loot
        int itemRoll = RANDOM.nextInt(0, 10);
        BiConsumer<Entity, Entity> onDeath;
        if (itemRoll == 0) {
            ItemDataGenerator itemDataGenerator = new ItemDataGenerator();
            Item item = itemDataGenerator.generateItemData();
            InventoryComponent ic = new InventoryComponent(1);
            monster.addComponent(ic);
            ic.add(item);
            onDeath =
                    (e, who) -> {
                        playMonsterDieSound();
                        new DropItemsInteraction().accept(e, who);
                    };
        } else {
            onDeath = (e, who) -> playMonsterDieSound();
        }
        return onDeath;
    }

    public static void playMonsterDieSound() {
        Sound dieSoundEffect;
        switch (RANDOM.nextInt(4)) {
            case 0 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_01.wav"));
            case 1 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_02.wav"));
            case 2 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_03.wav"));
            default -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_04.wav"));
        }
        long soundid = dieSoundEffect.play();
        dieSoundEffect.setLooping(soundid, false);
        dieSoundEffect.setVolume(soundid, 0.35f);
    }

    public static String randomMonsterIdleSound() {
        switch (RANDOM.nextInt(4)) {
            case 0 -> {
                return "sounds/monster1.wav";
            }
            case 1 -> {
                return "sounds/monster2.wav";
            }
            case 2 -> {
                return "sounds/monster3.wav";
            }
            default -> {
                return "sounds/monster4.wav";
            }
        }
    }

    public enum MonsterType {

        // texture,strength,skill
        IMP("imp", Strength.EASY, DamageType.PHYSICAL, AIStrat.RADIUS_COLLIDE_SELFDEFENT),
        CHORT("chort", Strength.EASY, DamageType.PHYSICAL, AIStrat.RADIUS_COLLIDE_RANGE),
        DEAMON("big_deamon", Strength.MEDIUM, DamageType.PHYSICAL, AIStrat.RADIUS_RANGE_RANGE),
        ZOMBIE("big_zombie", Strength.MEDIUM, DamageType.PHYSICAL, AIStrat.RADIUS_COLLIDE_RANGE),
        PUMPKIN_DUDE(
                "pumpkin_dude",
                Strength.MEDIUM,
                DamageType.PHYSICAL,
                AIStrat.RADIUS_COLLIDE_SELFDEFENT),
        DOC("doc", Strength.MEDIUM, DamageType.PHYSICAL, AIStrat.PATROL_RANGE_SELFDEFENT),
        GOBLIN("goblin", Strength.MEDIUM, DamageType.PHYSICAL, AIStrat.PATROL_COLLIDE_SELFDEFENT),
        ICE_ZOMBIE("ice_zombie", Strength.HARD, DamageType.PHYSICAL, AIStrat.RADIUS_RANGE_RANGE),
        OGRE("ogre", Strength.HARD, DamageType.PHYSICAL, AIStrat.RADIUS_COLLIDE_SELFDEFENT),
        ORC_SHAMAN("orc_shaman", Strength.HARD, DamageType.PHYSICAL, AIStrat.RADIUS_RANGE_RANGE),
        ORC_WARRIOR(
                "orc_warrior", Strength.HARD, DamageType.PHYSICAL, AIStrat.PATROL_COLLIDE_RANGE);

        private String textureName;
        private Strength strength;
        private DamageType collideDamageType;
        private AIStrat ai;

        MonsterType(
                String textureName, Strength strength, DamageType collideDamageType, AIStrat ai) {
            this.textureName = textureName;
            this.strength = strength;
            this.collideDamageType = collideDamageType;
            this.ai = ai;
        }

        public static MonsterType getRandomMonsterByStrength(Strength strength) {
            List<MonsterType> monstersWithStrength = Arrays.asList(values());
            Collections.shuffle(monstersWithStrength);
            for (MonsterType monster : monstersWithStrength) {
                if (monster.strength == strength) {
                    return monster;
                }
            }
            return IMP;
        }

        public static MonsterType getRandomMonster() {
            Random random = new Random();
            return values()[random.nextInt(values().length)];
        }

        // Getters for textureName and strength if needed

        public String getTextureName() {
            return textureName;
        }

        public Strength getStrength() {
            return strength;
        }
    }

    public enum Strength {
        EASY(10, 5f, 5, 3, 10f, 1000),
        MEDIUM(20, 7f, 10, 5, 14f, 750),
        HARD(30, 8f, 15, 10, 15f, 500);

        int hp;
        float speed;
        int collideDamage;
        int skillDamage;
        float skillSpeed;
        long skillCooldown;

        Strength(
                int hp,
                float speed,
                int collideDamage,
                int skillDamage,
                float skillSpeed,
                long skillCooldown) {
            this.hp = hp;
            this.speed = speed;
            this.collideDamage = collideDamage;
            this.skillDamage = skillDamage;
            this.skillSpeed = skillSpeed;
            this.skillCooldown = skillCooldown;
        }
    }

    public enum AIStrat {
        PATROL_RANGE_SELFDEFENT("PATROL", "RANGE", "SELFDEFENT"),
        PATROL_RANGE_RANGE("PATROL", "RANGE", "RANGE"),

        PATROL_COLLIDE_SELFDEFENT("PATROL", "COLIDE", "SELFDEFENT"),
        PATROL_COLLIDE_RANGE("PATROL", "COLIDE", "RANGE"),

        RADIUS_RANGE_SELFDEFENT("RADIUS", "RANGE", "SELFDEFENT"),
        RADIUS_RANGE_RANGE("RADIUS", "RANGE", "RANGE"),

        RADIUS_COLLIDE_SELFDEFENT("RADIUS", "COLLIDE", "SELFDEFENT"),
        RADIUS_COLLIDE_RANGE("RADIUS", "COLLIDE", "RANGE");

        String idle;
        String fight;
        String trans;

        AIStrat(String idle, String fight, String trans) {
            this.idle = idle;
            this.fight = fight;
            this.trans = trans;
        }
    }
}
