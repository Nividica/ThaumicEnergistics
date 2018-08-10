package thaumicenergistics.integration.appeng.grid;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.parts.IPart;
import appeng.me.GridAccessException;

/**
 * @author BrockWS
 */
@SuppressWarnings("ConstantConditions")
public class GridUtil {

    public static IGrid getGrid(IPart part) throws GridAccessException {
        IGridNode node = part.getGridNode();
        if (node == null)
            throw new GridAccessException();

        IGrid grid = node.getGrid();
        if (grid == null)
            throw new GridAccessException();
        return grid;
    }

    public static IEnergyGrid getEnergyGrid(IPart part) throws GridAccessException {
        IGrid grid = GridUtil.getGrid(part);
        if (grid == null)
            throw new GridAccessException();
        IEnergyGrid energy = grid.getCache(IEnergyGrid.class);
        if (energy == null)
            throw new GridAccessException();
        return energy;
    }
}
