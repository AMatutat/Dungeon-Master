package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import core.Entity;
import core.Game;
import core.crafting.Crafting;
import core.entities.EntityFactory;
import core.systems.*;
import core.utils.components.Debugger;

import java.io.IOException;

public class Main {

    private static final String BACKGROUND_MUSIC = "sounds/background.wav";

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Debugger debugger = new Debugger();
        // start the game
        configGame();

        onSetup();

        onFrame(debugger);

        // build and start game
        Game.run();
    }

    private static void onFrame(Debugger debugger) {
        Game.userOnFrame(debugger::execute);
    }

    private static void onSetup() {
        Game.userOnSetup(Main::basicSetup);
    }

    private static void setupMusic() {
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        backgroundMusic.setVolume(.1f);
    }

    private static void basicSetup() {
        createSystems();
        createHero();
        Crafting.loadRecipes();
        setupMusic();
    }

    private static void configGame() throws IOException {
        Game.loadConfig("dungeon_config.json", core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(false);
        Game.windowTitle("My Dungeon");
    }

    private static void createHero() {
        Entity hero = null;
        try {
            hero = (EntityFactory.newHero());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.add(hero);
        Game.hero(hero);
    }

    private static void createSystems() {
        Game.add(new CollisionSystem());
        Game.add(new AISystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
        Game.add(new HudSystem());
        Game.add(new SpikeSystem());
        Game.add(new IdleSoundSystem());
    }
}
