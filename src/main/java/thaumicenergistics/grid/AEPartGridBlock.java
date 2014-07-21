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

public class AEPartGridBlock implements IGridBlock
{

	protected AEColor gridColor;
	protected IGrid grid;
	protected int usedChannels;
	protected AEPartBase partHost;

	public AEPartGridBlock(AEPartBase partHost)
	{
		this.partHost = partHost;
	}

	@Override
	public EnumSet<ForgeDirection> getConnectableSides()
	{
		return null;
	}

	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return EnumSet.of( GridFlags.REQUIRE_CHANNEL );
	}

	public IMEMonitor<IAEFluidStack> getFluidMonitor()
	{
		IGridNode node = this.partHost.getGridNode();

		if ( node == null )
		{
			return null;
		}

		IGrid grid = node.getGrid();

		if ( grid == null )
		{
			return null;
		}

		IStorageGrid storageGrid = (IStorageGrid) grid.getCache( IStorageGrid.class );

		if ( storageGrid == null )
		{
			return null;
		}

		return storageGrid.getFluidInventory();
	}

	@Override
	public AEColor getGridColor()
	{
		return this.gridColor;
	}

	@Override
	public double getIdlePowerUsage()
	{
		return this.partHost.getPowerUsage();
	}

	@Override
	public DimensionalCoord getLocation()
	{
		return this.partHost.getLocation();
	}

	@Override
	public IGridHost getMachine()
	{
		return this.partHost;
	}

	@Override
	public ItemStack getMachineRepresentation()
	{
		return this.partHost.getItemStack( PartItemStack.Network );
	}

	@Override
	public void gridChanged()
	{
	}

	@Override
	public boolean isWorldAccessable()
	{
		return false;
	}

	@Override
	public void onGridNotification( GridNotification arg0 )
	{
	}

	@Override
	public final void setNetworkStatus( IGrid grid, int usedChannels )
	{
		this.grid = grid;
		this.usedChannels = usedChannels;
	}

}
