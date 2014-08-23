package thaumicenergistics.tileentities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectSource;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.common.FMLCommonHandler;

public class TileInfusionProvider
	extends TileProviderBase
	implements IAspectSource, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	public static final String TILE_ID = "TileInfusionProvider";

	/**
	 * List of aspects on the network
	 */
	protected List<AspectStack> aspectStackList = new ArrayList<AspectStack>();

	@Override
	protected void channelUpdated()
	{
		super.channelUpdated();

		// Is this server side?
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Remove ourself from any prior listener
			if( this.monitor != null )
			{
				this.monitor.removeListener( this );
			}

			// Get the new monitor
			if( this.getFluidMonitor() )
			{
				// Register this tile as a network monitor
				this.monitor.addListener( this, null );

				// Get the list of essentia on the network
				this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );

				// Mark that we need to update the client
				this.markForUpdate();
			}
		}
	}

	/**
	 * How much power does this require just to be active?
	 */
	@Override
	protected double getIdlePowerusage()
	{
		return 5.0;
	}

	@Override
	protected ItemStack getItemFromTile( Object obj )
	{
		// Return the itemstack the visually represents this tile
		return new ItemStack( BlockEnum.INFUSION_PROVIDER.getBlock(), 1 );

	}

	@Override
	public int addToContainer( Aspect tag, int amount )
	{
		// Ignored
		return 0;
	}

	@Override
	public int containerContains( Aspect tag )
	{
		// Ignored
		return 0;
	}

	@Override
	public boolean doesContainerAccept( Aspect tag )
	{
		// Ignored
		return false;
	}

	@Deprecated
	@Override
	public boolean doesContainerContain( AspectList ot )
	{
		// Ignored
		return false;
	}

	@Override
	public boolean doesContainerContainAmount( Aspect tag, int amount )
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
	public boolean isValid( Object verificationToken )
	{
		return true;
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
	public void postChange( IBaseMonitor<IAEFluidStack> monitor, IAEFluidStack change, BaseActionSource source )
	{
		// Ensure there was a change
		if( change == null )
		{
			return;
		}

		// Ensure the fluid is an essentia gas
		if( !( change.getFluid() instanceof GaseousEssentia ) )
		{
			return;
		}

		this.aspectStackList = EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( ( (IMEMonitor<IAEFluidStack>)monitor )
						.getStorageList() );

		// Mark that we need to update the client
		this.markForUpdate();
	}

	@Override
	public void setAspects( AspectList aspects )
	{
		// Ignored
	}

	@Override
	public boolean takeFromContainer( Aspect tag, int amount )
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
	public boolean takeFromContainer( AspectList ot )
	{
		// Ignored
		return false;
	}

}
