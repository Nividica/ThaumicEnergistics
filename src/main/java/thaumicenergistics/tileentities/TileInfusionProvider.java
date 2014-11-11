package thaumicenergistics.tileentities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import cpw.mods.fml.common.FMLCommonHandler;

// TODO: Display runes when providing essentia

public class TileInfusionProvider
	extends TileProviderBase
	implements IAspectSource, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	public static final String TILE_ID = "TileInfusionProvider";

	/**
	 * List of aspects on the network
	 */
	protected List<AspectStack> aspectStackList = new ArrayList<AspectStack>();

	/**
	 * How much power does this require just to be active?
	 */
	@Override
	protected double getIdlePowerusage()
	{
		return 5.0;
	}

	@Override
	protected ItemStack getItemFromTile( final Object obj )
	{
		// Return the itemstack the visually represents this tile
		return TEApi.instance().blocks().InfusionProvider.getStack();

	}

	@Override
	protected void onChannelUpdate()
	{
		// Is this server side?
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Remove ourself from any prior listener
			if( this.monitor != null )
			{
				this.monitor.removeListener( this );
			}

			IGrid grid;
			try
			{
				// Get the grid
				grid = this.gridProxy.getGrid();
			}
			catch( GridAccessException e )
			{
				// No grid
				return;
			}

			// Get the new monitor
			if( this.getFluidMonitor() )
			{
				// Register this tile as a network monitor
				this.monitor.addListener( this, grid );

				// Get the list of essentia on the network
				this.aspectStackList = EssentiaConversionHelper.instance.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );

				// Mark that we need to update the client
				this.markForUpdate();
			}
		}
	}

	@Override
	public int addToContainer( final Aspect tag, final int amount )
	{
		// Ignored
		return 0;
	}

	@Override
	public int containerContains( final Aspect tag )
	{
		// Ignored
		return 0;
	}

	@Override
	public boolean doesContainerAccept( final Aspect tag )
	{
		// Ignored
		return false;
	}

	@Deprecated
	@Override
	public boolean doesContainerContain( final AspectList ot )
	{
		// Ignored
		return false;
	}

	@Override
	public boolean doesContainerContainAmount( final Aspect tag, final int amount )
	{
		// Ignored
		return false;
	}

	@Override
	public AspectList getAspects()
	{
		// Ignored
		return null;
	}

	@Override
	public boolean isValid( final Object prevGrid )
	{
		IGrid grid;
		try
		{
			// Get the grid
			grid = this.gridProxy.getGrid();
		}
		catch( GridAccessException e )
		{
			// No grid
			return false;
		}

		// We are valid if our grid has not changed
		return( prevGrid == grid );
	}

	/**
	 * Called when our parent block is about to be destroyed.
	 */
	public void onBreakBlock()
	{
		// Do we have a monitor
		if( this.monitor != null )
		{
			// Unregister
			this.monitor.removeListener( this );
		}
	}

	@Override
	public void onListUpdate()
	{
		// Ignored
	}

	/**
	 * Called by the AE monitor when the network changes.
	 */
	@Override
	public void postChange( final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> changes, final BaseActionSource source )
	{
		// Ensure there was a change
		if( changes == null )
		{
			return;
		}

		// Ensure one of the changes is an essentia gas
		for( IAEFluidStack change : changes )
		{
			if( ( change.getFluid() instanceof GaseousEssentia ) )
			{
				// Update the aspect list
				this.aspectStackList = EssentiaConversionHelper.instance
								.convertIIAEFluidStackListToAspectStackList( ( (IMEMonitor<IAEFluidStack>)monitor ).getStorageList() );

				// Mark that we need to update the client
				this.markForUpdate();

				// Stop searching
				break;
			}
		}
	}

	@Override
	public void setAspects( final AspectList aspects )
	{
		// Ignored
	}

	@Override
	public boolean takeFromContainer( final Aspect tag, final int amount )
	{
		// Can we extract the essentia from the network?
		if( this.extractEssentiaFromNetwork( tag, amount, true ) == amount )
		{
			return true;
		}

		return false;
	}

	@Deprecated
	@Override
	public boolean takeFromContainer( final AspectList ot )
	{
		// Ignored
		return false;
	}

}
