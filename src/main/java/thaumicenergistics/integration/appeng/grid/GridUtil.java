package thaumicenergistics.integration.appeng.grid;

import javax.annotation.Nonnull;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;

/**
 * Grid helper class
 *
 * @author BrockWS
 */
@SuppressWarnings("ConstantConditions")
public class GridUtil {

    public static IEnergyGrid getEnergyGrid(@Nonnull IGridHost host) throws GridAccessException {
        return (IEnergyGrid) GridUtil.getCache(host, IEnergyGrid.class);
    }

    public static IEnergyGrid getEnergyGrid(@Nonnull IGridNode node) throws GridAccessException {
        return (IEnergyGrid) GridUtil.getCache(node, IEnergyGrid.class);
    }

    public static IEnergyGrid getEnergyGrid(@Nonnull IGrid grid) throws GridAccessException {
        return (IEnergyGrid) GridUtil.getCache(grid, IEnergyGrid.class);
    }

    public static IStorageGrid getStorageGrid(@Nonnull IGridHost host) throws GridAccessException {
        return (IStorageGrid) GridUtil.getCache(host, IStorageGrid.class);
    }

    public static IStorageGrid getStorageGrid(@Nonnull IGridNode node) throws GridAccessException {
        return (IStorageGrid) GridUtil.getCache(node, IStorageGrid.class);
    }

    public static IGridCache getCache(@Nonnull IGridHost host, @Nonnull Class<? extends IGridCache> clazz) throws GridAccessException {
        return GridUtil.getCache(GridUtil.getGrid(host), clazz);
    }

    public static IGridCache getCache(@Nonnull IGridNode node, @Nonnull Class<? extends IGridCache> clazz) throws GridAccessException {
        return GridUtil.getCache(GridUtil.getGrid(node), clazz);
    }

    public static IGridCache getCache(@Nonnull IGrid grid, @Nonnull Class<? extends IGridCache> clazz) throws GridAccessException {
        IGridCache cache = grid.getCache(clazz);
        if (cache == null)
            throw new GridAccessException();
        return cache;
    }

    public static IGrid getGrid(@Nonnull IGridHost host) throws GridAccessException {
        IGridNode node = host.getGridNode(AEPartLocation.UP);
        if (node == null)
            throw new GridAccessException();
        return GridUtil.getGrid(node);
    }

    public static IGrid getGrid(@Nonnull IGridNode node) throws GridAccessException {
        IGrid grid = node.getGrid();
        if (grid == null)
            throw new GridAccessException();
        return grid;
    }
}
