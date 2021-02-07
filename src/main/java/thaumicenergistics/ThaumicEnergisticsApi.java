package thaumicenergistics;

import thaumicenergistics.api.*;
import thaumicenergistics.config.ThEConfig;
import thaumicenergistics.init.*;
import thaumicenergistics.lang.ThELang;
import thaumicenergistics.upgrade.ThEUpgrades;

/**
 * @author BrockWS
 */
public class ThaumicEnergisticsApi implements IThEApi {

    private static IThEApi INSTANCE;
    private final IThESounds sounds;
    private final IThEItems items;
    private final IThEBlocks blocks;
    private final IThEUpgrades upgrades;
    private final IThEConfig config;
    private final IThELang lang;
    private final IThETextures textures;

    private ThaumicEnergisticsApi() {
        this.sounds = new ThESounds();
        this.items = new ThEItems();
        this.blocks = new ThEBlocks();
        this.upgrades = new ThEUpgrades(this.items());
        this.config = new ThEConfig();
        this.lang = new ThELang();
        this.textures = new ThETextures();
    }

    public static IThEApi instance() {
        if (INSTANCE == null)
            INSTANCE = new ThaumicEnergisticsApi();
        return INSTANCE;
    }

    @Override
    public IThEItems items() {
        return this.items;
    }

    @Override
    public IThEBlocks blocks() {
        return this.blocks;
    }

    @Override
    public IThEUpgrades upgrades() {
        return this.upgrades;
    }

    @Override
    public IThEConfig config() {
        return this.config;
    }

    @Override
    public IThELang lang() {
        return this.lang;
    }

    @Override
    public IThETextures textures() {
        return this.textures;
    }

    @Override
    public IThESounds sounds() {
        return this.sounds;
    }
}
