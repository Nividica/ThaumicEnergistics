package thaumicenergistics.common.tiles.abstraction;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.common.integration.IWailaSource;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.tiles.TileEssentiaProvider;
import thaumicenergistics.common.tiles.TileInfusionProvider;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.localization.WailaText;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base class of {@link TileEssentiaProvider} and {@link TileInfusionProvider}.
 * 
 * @author Nividica
 * 
 */
public abstract class TileProviderBase
	extends AENetworkTile
	implements IColorableTile, IWailaSource
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
	 * ME monitor that watches for changes in essentia.
	 */
	protected IMEEssentiaMonitor monitor = null;

	/**
	 * True if the provider is connected and powered.
	 */
	protected boolean isActive;

	/**
	 * True when the color applicator has been used on the provider.
	 */
	protected boolean isColorForced = false;

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
		if( this.getEssentiaMonitor() )
		{
			// Request the essentia
			long amountExtracted = this.monitor.extractEssentia( wantedAspect, wantedAmount, Actionable.SIMULATE, this.asMachineSource, true );

			// Was any essentia extracted?
			if( amountExtracted == 0 )
			{
				// Nothing extracted.
				return 0;
			}

			// Are we in match mode?
			else if( mustMatch )
			{
				// Does the amount match how much we want?
				if( amountExtracted != wantedAmount )
				{
					// Could not provide enough essentia
					return 0;
				}
			}

			// Extract the essentia
			this.monitor.extractEssentia( wantedAspect, wantedAmount, Actionable.MODULATE, this.asMachineSource, true );

			// Return how much was extracted
			return (int)amountExtracted;
		}

		// No monitor
		return 0;

	}

	/**
	 * Gets the essentia monitor from the network.
	 * Returns true if a monitor was retrieved.
	 * 
	 * @return
	 */
	protected boolean getEssentiaMonitor()
	{
		IMEEssentiaMonitor essentiaMonitor = null;
		IGrid grid = null;

		// Get the grid node
		IGridNode node = this.getProxy().getNode();

		// Ensure we have the node
		if( node != null )
		{
			// Get the grid that node is connected to
			grid = node.getGrid();

			// Is there a grid?
			if( grid != null )
			{
				// Get the monitor
				essentiaMonitor = (IMEEssentiaMonitor)grid.getCache( IEssentiaGrid.class );
			}
		}

		// Set the monitor
		this.monitor = essentiaMonitor;

		// Return true if the monitor is not null
		return( this.monitor != null );
	}

	protected abstract double getIdlePowerusage();

	@Override
	protected abstract ItemStack getItemFromTile( Object obj );

	/**
	 * Called when the power goes on or off.
	 * 
	 * @param isPowered
	 */
	protected void onPowerChange( final boolean isPowered )
	{
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
		this.getProxy().setColor( gridColor );

		// Are we server side?
		if( EffectiveSide.isServerSide() )
		{
			/*
			// Get the grid node
			IGridNode gridNode = this.getProxy().getNode();

			// Do we have a grid node?
			if( gridNode != null )
			{
				// Update the grid node
				this.getProxy().getNode().updateState();
			}
			*/

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

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Update the monitor
			this.getEssentiaMonitor();
		}

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
		AEColor currentColor = this.getProxy().getColor();

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

	/**
	 * Returns how much of the specified aspect is in the network.
	 * 
	 * @param searchAspect
	 * @return
	 */
	public long getAspectAmountInNetwork( final Aspect searchAspect )
	{
		// Ensure there is a monitor
		if( this.getEssentiaMonitor() )
		{
			// Return the amount in the network
			return this.monitor.getEssentiaAmount( searchAspect );
		}

		// No monitor.
		return 0;
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
		return this.getProxy().getColor();
	}

	public AEColor getGridColor()
	{
		return this.getProxy().getGridColor();
	}

	/**
	 * Gets the machine source for the provider.
	 * 
	 * @return
	 */
	public MachineSource getMachineSource()
	{
		return this.asMachineSource;
	}

	public boolean isActive()
	{
		// Are we server side?
		if( EffectiveSide.isServerSide() )
		{
			boolean prevActive = this.isActive;

			// Do we have a proxy and grid node?
			if( ( this.getProxy() != null ) && ( this.getProxy().getNode() != null ) )
			{
				// Get the grid node activity
				this.isActive = this.getProxy().getNode().isActive();

				// Did the power state change?
				if( prevActive != this.isActive )
				{
					// Send event
					this.onPowerChange( this.isActive );
				}
			}
		}

		return this.isActive;
	}

	/**
	 * Called when our parent block is about to be destroyed.
	 */
	public void onBreakBlock()
	{
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
		this.getProxy().setOwner( player );
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
			this.getProxy().setFlags( GridFlags.REQUIRE_CHANNEL );

			// Set the idle power usage
			this.getProxy().setIdlePowerUsage( this.getIdlePowerusage() );
		}
	}

}
