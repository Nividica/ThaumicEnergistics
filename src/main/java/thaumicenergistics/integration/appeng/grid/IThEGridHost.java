package thaumicenergistics.integration.appeng.grid;

import appeng.api.networking.IGridHost;
import appeng.api.util.DimensionalCoord;

/**
 * @author BrockWS
 */
public interface IThEGridHost extends IGridHost {

    DimensionalCoord getLocation();

    void gridChanged();
}
