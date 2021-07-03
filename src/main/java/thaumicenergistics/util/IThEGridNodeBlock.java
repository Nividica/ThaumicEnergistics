package thaumicenergistics.util;

import appeng.api.networking.IGridNode;
import thaumicenergistics.integration.appeng.grid.ThEGridBlock;

/**
 * @author Alex811
 */
public interface IThEGridNodeBlock {
    ThEGridBlock getGridBlock();

    IGridNode getGridNode();
}
