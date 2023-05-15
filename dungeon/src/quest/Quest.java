package quest;

import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.utils.controller.ScreenController;

import dslToGame.QuestConfig;

public abstract class Quest {
    protected QuestConfig questConfig;
    protected IGenerator generator;
    protected ILevel root;
    protected String questText;

    protected ScreenController sc;

    protected int maxscore;

    public Quest(QuestConfig questConfig, ScreenController sc) {
        this.questConfig = questConfig;
        this.maxscore = questConfig.questPoints();
        this.questText = questConfig.questDesc();
        this.sc = sc;
    }

    public IGenerator getGenerator() {
        return generator;
    }

    public void setRootLevel(ILevel root) {
        this.root = root;
    }

    public abstract void addQuestObjectsToLevels();

    public abstract void addQuestUIElements();

    public abstract int evaluateUserPerformance();

    public abstract void onLevelLoad(ILevel currentLevel);
}
