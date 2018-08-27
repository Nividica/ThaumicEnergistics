package thaumicenergistics;

import thaumicenergistics.api.IThEApi;
import thaumicenergistics.api.IThEBlocks;
import thaumicenergistics.api.IThEItems;
import thaumicenergistics.init.ThEBlocks;
import thaumicenergistics.init.ThEItems;

/**
 * @author BrockWS
 */
public class ThaumicEnergisticsApi implements IThEApi {

    private static IThEApi INSTANCE;
    private IThEItems items;
    private IThEBlocks blocks;

    private ThaumicEnergisticsApi() {
        this.items = new ThEItems();
        this.blocks = new ThEBlocks();
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
}
