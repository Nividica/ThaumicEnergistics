package thaumicenergistics.grid;

import java.util.EnumSet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.parts.AEPartBase;
import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;

/**
 * AE Gridblock used for all my AE parts. 
 * @author Nividica
 *
 */
public class AEPartGridBlock implements IGridBlock
{
	/**
	 * The color of the grid we are attached to
	 */
	protected AEColor gridColor;
	
	/**
	 * The grid we are attached to
	 */
	protected IGrid grid;
	
	/**
	 * The number of channels used in the grid? I think.
	 */
	protected int usedChannels;
	
	/**
	 * The part using this gridblock.
	 */
	protected AEPartBase part;

	/**
	 * Create the gridblock for the specified part.
	 * @param part
	 */
	public AEPartGridBlock(AEPartBase part)
	{
		this.part = part;
	}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides()
	{
		// Ignored.
		return null;
	}

	/**
	 * Return that we require a channel to function.
	 */
	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return EnumSet.of( GridFlags.REQUIRE_CHANNEL );
	}

	/**
	 * Gets the AE fluid monitor for the grid.
	 * @return Monitor if valid grid, null otherwise.
	 */
	public IMEMonitor<IAEFluidStack> getFluidMonitor()
	{
		// Get the grid node
		IGridNode node = this.part.getGridNode();

		// Do we have a grid node?
		if ( node == null )
		{
			return null;
		}

		// Get the grid.
		IGrid grid = node.getGrid();

		// Do we have a grid?
		if ( grid == null )
		{
			return null;
		}

		// Get the storage grid from the cache.
		IStorageGrid storageGrid = (IStorageGrid) grid.getCache( IStorageGrid.class );

		// Do we have a storage grid?
		if ( storageGrid == null )
		{
			return null;
		}

		// Return the storage grid's fluid monitor.
		return storageGrid.getFluidInventory();
	}

	/**
	 * Returns the color of the grid.
	 */
	@Override
	public AEColor getGridColor()
	{
		// Do we have a grid color?
		if( this.gridColor != null )
		{
			// Return the grid color
			return this.gridColor;
		}
		
		// Invalid color, return transparent.
		return AEColor.Transparent;
	}

	/**
	 * Gets how much power the part is using.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return this.part.getPowerUsage();
	}

	/**
	 * Gets the location of the part.
	 */
	@Override
	public DimensionalCoord getLocation()
	{
		return this.part.getLocation();
	}

	/**
	 * Gets the part
	 */
	@Override
	public IGridHost getMachine()
	{
		return this.part;
	}

	/**
	 * Gets the an itemstack based on the parts state.
	 */
	@Override
	public ItemStack getMachineRepresentation()
	{
		return this.part.getItemStack( PartItemStack.Network );
	}

	@Override
	public void gridChanged()
	{
		// Ignored
	}

	/**
	 * Parts are not world accessable
	 */
	@Override
	public boolean isWorldAccessable()
	{
		return false;
	}

	@Override
	public void onGridNotification( GridNotification notification )
	{
		// Ignored
	}

	/**
	 * Called to update the grid and the channels used.
	 */
	@Override
	public final void setNetworkStatus( IGrid grid, int usedChannels )
	{
		// Set the grid.
		this.grid = grid;
		
		// Set the channels used.
		this.usedChannels = usedChannels;
	}

}
