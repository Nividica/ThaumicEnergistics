package thaumicenergistics.tileentities;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectCache;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.integration.IWailaSource;
import thaumicenergistics.integration.tc.EssentiaConversionHelper;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.localization.WailaText;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileProviderBase
	extends AENetworkTile
	implements IColorableTile, IWailaSource, IMEMonitorHandlerReceiver<IAEFluidStack>
{
	/**
	 * NBT keys
	 */
	protected static final String NBT_KEY_COLOR = "TEColor", NBT_KEY_ATTACHMENT = "TEAttachSide", NBT_KEY_ISCOLORFORCED = "ColorForced";

	/**
	 * Network source representing the provider.
	 */
	private MachineSource asMachineSource;

	/**
	 * ForgeDirection ordinal of our attachment side.
	 */
	protected int attachmentSide;

	/**
	 * ME monitor that watches for changes in fluids.
	 */
	protected IMEMonitor<IAEFluidStack> monitor = null;

	/**
	 * True if the provider is connected and powered.
	 */
	protected boolean isActive;

	/**
	 * True when the color applicator has been used on the provider.
	 */
	protected boolean isColorForced = false;

	/**
	 * Cached aspects in the network.
	 */
	private AspectCache aspectCache = new AspectCache();

	/**
	 * Ensures thread safety on the cache.
	 */
	private Object cacheLock = new Object();

	/**
	 * Contains a cache of the aspects and amounts reported to ComputerCraft.
	 */
	private Object[] ccListCache = null;

	public TileProviderBase()
	{
		// Create the source
		this.asMachineSource = new MachineSource( this );
	}

	/**
	 * Returns an array with the AE colors of any neighbor tiles.
	 * Index is side.
	 * 
	 * @return
	 */
	private AEColor[] getNeighborCableColors()
	{
		AEColor[] sideColors = new AEColor[6];

		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			// Get the tile entity on the current side
			TileEntity tileEntity = this.worldObj.getTileEntity( this.xCoord + side.offsetX, this.yCoord + side.offsetY, this.zCoord + side.offsetZ );

			// Did we get an entity?
			if( tileEntity == null )
			{
				continue;
			}

			// Is that entity a cable?
			if( tileEntity instanceof TileCableBus )
			{
				// Set the color
				sideColors[side.ordinal()] = ( (TileCableBus)tileEntity ).getColor();
			}

		}

		return sideColors;
	}

	/**
	 * Returns how much was extracted from the network.
	 * 
	 * @param wantedAspect
	 * @param wantedAmount
	 * @param mustMatch
	 * @return
	 */
	protected int extractEssentiaFromNetwork( final Aspect wantedAspect, final int wantedAmount, final boolean mustMatch )
	{
		// Ensure we have a monitor
		if( this.getFluidMonitor() )
		{
			// Get the gas version of the aspect
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( wantedAspect );

			// Is there a fluid version of the aspect?
			if( essentiaGas == null )
			{
				return 0;
			}

			IAEFluidStack request = EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( essentiaGas, wantedAmount );

			// Simulate the extraction
			IAEFluidStack fluidStack = this.monitor.extractItems( request, Actionable.SIMULATE, this.asMachineSource );

			// Were we able to extract any?
			if( fluidStack == null )
			{
				return 0;
			}
			// Are we in match mode?
			else if( mustMatch )
			{
				// Does the amount match how much we want?
				if( fluidStack.getStackSize() != EssentiaConversionHelper.INSTANCE.convertEssentiaAmountToFluidAmount( wantedAmount ) )
				{
					// Could not provide enough essentia
					return 0;
				}
			}

			// Take from the network
			this.monitor.extractItems( request, Actionable.MODULATE, this.asMachineSource );

			// Return how much was extracted
			return (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );
		}

		return 0;

	}

	/**
	 * Returns how much of the specified aspect is in the network.
	 * 
	 * @param searchAspect
	 * @return
	 */
	protected long getAspectAmountInNetwork( final Aspect searchAspect )
	{
		synchronized( this.cacheLock )
		{
			return this.aspectCache.getAspectAmount( searchAspect );
		}
	}

	/**
	 * Sets the fluid monitor field to the grids fluid monitor if one is
	 * available.
	 * Returns true if a monitor was retrieved.
	 * 
	 * @return
	 */
	protected boolean getFluidMonitor()
	{
		// Get the grid node
		IGridNode node = this.gridProxy.getNode();

		// Ensure we have the node
		if( node == null )
		{
			return false;
		}

		// Get the grid that node is connected to
		IGrid grid = node.getGrid();

		// Is there a grid?
		if( grid == null )
		{
			return false;
		}

		// Access the storage grid
		IStorageGrid storageGrid = (IStorageGrid)grid.getCache( IStorageGrid.class );

		// Get the monitor
		IMEMonitor<IAEFluidStack> gMonitor = storageGrid.getFluidInventory();

		// Is the previous monitor invalid?
		if( ( this.monitor != null ) && ( ( gMonitor == null ) || ( !this.monitor.equals( gMonitor ) ) ) )
		{
			// Remove the listener
			this.monitor.removeListener( this );

			// Attach a new listener
			if( gMonitor != null )
			{
				gMonitor.addListener( this, grid );
			}
		}

		// Set the monitor
		this.monitor = gMonitor;

		// Return true if the monitor is not null
		return( this.monitor != null );
	}

	protected abstract double getIdlePowerusage();

	@Override
	protected abstract ItemStack getItemFromTile( Object obj );

	/**
	 * Injects essentia into the ME network.
	 * 
	 * @param aspect
	 * @param amount
	 * @param mode
	 * @return
	 */
	protected int injectEssentiaIntoNetwork( final Aspect aspect, final int amount, final Actionable mode )
	{
		// Ensure there is a request
		if( amount == 0 )
		{
			return 0;
		}

		// Ensure we have a monitor
		if( this.getFluidMonitor() )
		{
			// Get the gas version of the aspect
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( aspect );

			// Is there a fluid version of the aspect?
			if( essentiaGas == null )
			{
				return 0;
			}

			IAEFluidStack request = EssentiaConversionHelper.INSTANCE.createAEFluidStackInEssentiaUnits( essentiaGas, amount );

			// Simulate the injection
			IAEFluidStack fluidStack = this.monitor.injectItems( request, mode, this.asMachineSource );

			// Was all injected?
			if( fluidStack == null )
			{
				return amount;
			}

			// Return the amount not injected
			return amount - (int)EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );
		}

		// No monitor
		return 0;

	}

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
				synchronized( this.cacheLock )
				{
					EssentiaConversionHelper.INSTANCE.updateCacheToMatchIAEFluidStackList( this.aspectCache, this.monitor.getStorageList() );
				}
			}
		}
	}

	/**
	 * Sets the color of the provider.
	 * This does not set the isColorForced flag to true.
	 * 
	 * @param gridColor
	 */
	protected void setProviderColor( final AEColor gridColor )
	{
		// Set our color to match
		this.gridProxy.myColor = gridColor;

		// Are we server side?
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Get the grid node
			IGridNode gridNode = this.gridProxy.getNode();

			// Do we have a grid node?
			if( gridNode != null )
			{
				// Update the grid node
				this.gridProxy.getNode().updateState();
			}

			// Mark the tile as needing updates and to be saved
			this.markForUpdate();
			this.saveChanges();
		}
	}

	/**
	 * Adds the power state and color to a Waila tooltip
	 */
	@Override
	public void addWailaInformation( final List<String> tooltip )
	{
		// Is it active?
		if( this.isActive() )
		{
			// Get activity string from AppEng2
			tooltip.add( WailaText.DeviceOnline.getLocal() );
		}
		else
		{
			// Get activity string from AppEng2
			tooltip.add( WailaText.DeviceOffline.getLocal() );
		}

		// Add the color
		tooltip.add( "Color: " + this.getColor().toString() );
	}

	@MENetworkEventSubscribe
	public final void channelEvent( final MENetworkChannelsChanged event )
	{
		// Check that our color is still valid
		this.checkGridConnectionColor();

		// Call subclass
		this.onChannelUpdate();

		// Mark for update
		this.markForUpdate();
	}

	public void checkGridConnectionColor()
	{
		// Are we server side and with a world?
		if( FMLCommonHandler.instance().getEffectiveSide().isClient() || ( this.worldObj == null ) )
		{
			// Nothing to do
			return;
		}

		// Is our color forced?
		if( this.isColorForced )
		{
			// Do not change colors.
			return;
		}

		// Get the colors of our neighbors
		AEColor[] sideColors = this.getNeighborCableColors();

		// Get our current color
		AEColor currentColor = this.gridProxy.myColor;

		// Are we attached to a side?
		if( this.attachmentSide != ForgeDirection.UNKNOWN.ordinal() )
		{
			// Does our attached side still exist
			if( sideColors[this.attachmentSide] != null )
			{
				// Do we match it's color?
				if( sideColors[this.attachmentSide] == currentColor )
				{
					// Nothing to change
					return;
				}

				// Set our color to match
				this.setProviderColor( sideColors[this.attachmentSide] );

				return;
			}
		}

		// Are any of the other sides the same color?
		for( int index = 0; index < 6; index++ )
		{
			if( sideColors[index] != null )
			{
				// Does the current side match our color?
				if( sideColors[index] == currentColor )
				{
					// Found another cable with the same color, lets attach to it
					this.attachmentSide = index;

					// Mark for a save
					this.saveChanges();

					return;
				}
				// Are we transparent?
				else if( currentColor == AEColor.Transparent )
				{
					// Attach to this cable
					this.attachmentSide = index;

					// Take on its color
					this.setProviderColor( sideColors[index] );

					return;
				}
			}
		}

		// No cables match our color, set attachment to unknown
		this.attachmentSide = ForgeDirection.UNKNOWN.ordinal();

		// Set color to transparent
		this.setProviderColor( AEColor.Transparent );

	}

	@Override
	public AECableType getCableConnectionType( final ForgeDirection direction )
	{
		return AECableType.SMART;
	}

	/**
	 * Get's the color of the provider
	 */
	@Override
	public AEColor getColor()
	{
		return this.gridProxy.myColor;
	}

	public AEColor getGridColor()
	{
		return this.gridProxy.getGridColor();
	}

	/**
	 * Gets the aspects in the network and stores them in the object array
	 * in AspectName-Amount pairs.
	 * This is included for ComputerCraft integration.
	 * 
	 * @param results
	 */
	public Object[] getOrderedAspectAmounts()
	{
		// Is the provider active?
		if( !this.isActive() )
		{
			// Inactive
			return null;
		}

		// Is the cache invalid?
		if( ( this.ccListCache == null ) || ( this.aspectCache.flagAmountsModified ) || ( this.aspectCache.flagAspectsModified ) )
		{
			// Something needs updating
			synchronized( this.cacheLock )
			{
				// Trim empty entries
				this.aspectCache.removeEmpty();

				// Full update?
				if( this.aspectCache.flagAspectsModified )
				{
					// Get the names
					String[] tags = this.aspectCache.getOrderedAspectTags();

					// Create the results array
					this.ccListCache = new Object[tags.length * 2];

					// Add the names to every other index, starting at 0
					for( int i = 0; i < tags.length; ++i )
					{
						this.ccListCache[i * 2] = tags[i];
					}

					// Mark amounts as invalid
					this.aspectCache.flagAmountsModified = true;
				}

				// Amounts?
				if( this.aspectCache.flagAmountsModified )
				{
					// Add the amounts to every other index, starting at 1
					for( int i = 1; i < this.ccListCache.length; i += 2 )
					{
						this.ccListCache[i] = this.aspectCache.getAspectAmount( (String)this.ccListCache[i - 1] );
					}

					// Mark cache as valid
					this.aspectCache.flagAmountsModified = false;
				}

				// If a full update occurred, clean up the names
				if( this.aspectCache.flagAspectsModified )
				{
					for( int i = 0; i < this.ccListCache.length; i += 2 )
					{
						this.ccListCache[i] = Aspect.getAspect( (String)this.ccListCache[i] ).getName();
					}

					// Mark aspects as valid
					this.aspectCache.flagAspectsModified = false;
				}

			}
		}

		return this.ccListCache;
	}

	public boolean isActive()
	{
		// Are we server side?
		if( EffectiveSide.isServerSide() )
		{
			// Do we have a proxy and grid node?
			if( ( this.gridProxy != null ) && ( this.gridProxy.getNode() != null ) )
			{
				// Get the grid node activity
				this.isActive = this.gridProxy.getNode().isActive();
			}
		}

		return this.isActive;
	}

	@Override
	public final boolean isValid( final Object prevGrid )
	{
		try
		{
			// Get the local grid
			IGrid localGrid = this.gridProxy.getGrid();

			// Listener is valid only if the grid has not changed.
			return( localGrid.equals( prevGrid ) );
		}
		catch( GridAccessException gE )
		{
			// This listener is no longer valid.
		}

		return false;
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
	public final void onListUpdate()
	{
		// Update the cache
		synchronized( this.cacheLock )
		{
			EssentiaConversionHelper.INSTANCE.updateCacheToMatchIAEFluidStackList( this.aspectCache, this.monitor.getStorageList() );
		}
	}

	@TileEvent(TileEventType.WORLD_NBT_READ)
	public void onLoadNBT( final NBTTagCompound data )
	{
		int attachmentSideFromNBT = ForgeDirection.UNKNOWN.ordinal();

		// Do we have the forced key?
		if( data.hasKey( TileProviderBase.NBT_KEY_ISCOLORFORCED ) )
		{
			this.isColorForced = data.getBoolean( TileProviderBase.NBT_KEY_ISCOLORFORCED );
		}

		// Do we have the color key?
		if( data.hasKey( TileProviderBase.NBT_KEY_COLOR ) )
		{
			// Read the color from the tag
			this.setProviderColor( EnumCache.AE_COLOR[data.getInteger( TileProviderBase.NBT_KEY_COLOR )] );
		}

		// Do we have the attachment key?
		if( data.hasKey( TileProviderBase.NBT_KEY_ATTACHMENT ) )
		{
			// Read the attachment side
			attachmentSideFromNBT = data.getInteger( TileProviderBase.NBT_KEY_ATTACHMENT );
		}

		// Setup the tile
		this.setupProvider( attachmentSideFromNBT );
	}

	@Override
	public void onReady()
	{
		// Call super
		super.onReady();

		// Check grid color
		this.checkGridConnectionColor();
	}

	@TileEvent(TileEventType.NETWORK_READ)
	@SideOnly(Side.CLIENT)
	public boolean onReceiveNetworkData( final ByteBuf data )
	{
		// Read the color from the stream
		this.setProviderColor( EnumCache.AE_COLOR[data.readInt()] );

		// Read the activity
		this.isActive = data.readBoolean();

		return true;
	}

	@TileEvent(TileEventType.WORLD_NBT_WRITE)
	public void onSaveNBT( final NBTTagCompound data )
	{
		// Write our color to the tag
		data.setInteger( TileProviderBase.NBT_KEY_COLOR, this.getGridColor().ordinal() );

		// Write the attachment side to the tag
		data.setInteger( TileProviderBase.NBT_KEY_ATTACHMENT, this.attachmentSide );

		// Write the forced color flag
		data.setBoolean( TileProviderBase.NBT_KEY_ISCOLORFORCED, this.isColorForced );
	}

	@TileEvent(TileEventType.NETWORK_WRITE)
	public void onSendNetworkData( final ByteBuf data ) throws IOException
	{
		// Write the color data to the stream
		data.writeInt( this.getGridColor().ordinal() );

		// Write the activity to the stream
		data.writeBoolean( this.isActive() );
	}

	/**
	 * Called by the AE monitor when the network changes.
	 */
	@Override
	public final void postChange( final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> changes, final BaseActionSource source )
	{
		// Ensure there was a change
		if( changes == null )
		{
			return;
		}

		synchronized( this.cacheLock )
		{
			// Search the changes for essentia gas
			for( IAEFluidStack change : changes )
			{
				// Is the change an essentia gas?
				if( ( change.getFluid() instanceof GaseousEssentia ) )
				{
					// Update the cache
					this.aspectCache.changeAspect( ( (GaseousEssentia)change.getFluid() ).getAspect(),
						EssentiaConversionHelper.INSTANCE.convertFluidAmountToEssentiaAmount( change.getStackSize() ) );
				}
			}
		}
	}

	@MENetworkEventSubscribe
	public final void powerEvent( final MENetworkPowerStatusChange event )
	{
		this.markForUpdate();
	}

	/**
	 * Forces a color change for the provider.
	 * Called when the provider's color is changed via the ColorApplicator item.
	 */
	@Override
	public boolean recolourBlock( final ForgeDirection side, final AEColor color, final EntityPlayer player )
	{
		// Mark our color as forced
		this.isColorForced = true;

		// Set our color
		this.setProviderColor( color );

		return true;
	}

	/**
	 * Sets the owner of this tile.
	 * 
	 * @param player
	 */
	public void setOwner( final EntityPlayer player )
	{
		this.gridProxy.setOwner( player );
	}

	/**
	 * Configures the provider based on the specified
	 * attachment side.
	 * 
	 * @param attachmentSide
	 */
	public void setupProvider( final int attachmentSide )
	{
		// Ignored on client side
		if( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Set which side we are attached to
			this.attachmentSide = attachmentSide;

			// Set that we require a channel
			this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );

			// Set the idle power usage
			this.gridProxy.setIdlePowerUsage( this.getIdlePowerusage() );
		}
	}

}
