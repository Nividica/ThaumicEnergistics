package thaumicenergistics.tileentities;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class TileProviderBase
	extends AENetworkTile
{
	protected static final String NBTKEY_COLOR = "TEColor";
	protected static final String NBTKEY_ATTACHMENT = "TEAttachSide";

	protected int attachmentSide;

	protected IMEMonitor<IAEFluidStack> monitor = null;

	protected boolean isActive;

	private AETileEventHandler eventHandler = new AETileEventHandler( TileEventType.WORLD_NBT, TileEventType.NETWORK )
	{
		@Override
		public void writeToNBT( NBTTagCompound data )
		{
			// Write our color to the tag
			data.setInteger( TileProviderBase.NBTKEY_COLOR, TileProviderBase.this.getGridColor().ordinal() );

			// Write the attachment side to the tag
			data.setInteger( TileProviderBase.NBTKEY_ATTACHMENT, TileProviderBase.this.attachmentSide );
		}

		@Override
		public void readFromNBT( NBTTagCompound data )
		{
			int attachmentSideFromNBT = ForgeDirection.UNKNOWN.ordinal();

			// Do we have the color key?
			if( data.hasKey( TileProviderBase.NBTKEY_COLOR ) )
			{
				// Read the color from the tag
				TileProviderBase.this.setGridColor( AEColor.values()[data.getInteger( TileProviderBase.NBTKEY_COLOR )] );
			}

			// Do we have the attachment key?
			if( data.hasKey( TileProviderBase.NBTKEY_ATTACHMENT ) )
			{
				// Read the attachment side
				attachmentSideFromNBT = data.getInteger( TileProviderBase.NBTKEY_ATTACHMENT );
			}

			// Setup the tile
			TileProviderBase.this.setupProvider( attachmentSideFromNBT );
		}

		@Override
		public void writeToStream( ByteBuf data ) throws IOException
		{
			// Write the color data to the stream
			data.writeInt( TileProviderBase.this.getGridColor().ordinal() );

			// Write the activity to the stream
			data.writeBoolean( TileProviderBase.this.isActive() );
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean readFromStream( ByteBuf data ) throws IOException
		{
			// Read the color from the stream
			TileProviderBase.this.setGridColor( AEColor.values()[data.readInt()] );

			// Read the activity
			TileProviderBase.this.isActive = data.readBoolean();

			return true;
		}
	};

	public TileProviderBase()
	{
		// Register our event handler
		this.addNewHandler( this.eventHandler );
	}

	/**
	 * Configures the provider based on the specified
	 * attachment side.
	 * @param attachmentSide
	 */
	public void setupProvider( int attachmentSide )
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

	@Override
	public void onReady()
	{
		super.onReady();
		this.checkGridConnectionColor();
	}

	@Override
	protected abstract ItemStack getItemFromTile( Object obj );
	
	protected abstract double getIdlePowerusage();

	@Override
	public AECableType getCableConnectionType( ForgeDirection direction )
	{
		return AECableType.SMART;
	}

	public AEColor getGridColor()
	{
		return this.gridProxy.getGridColor();
	}

	public void setGridColor( AEColor gridColor )
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

	public void checkGridConnectionColor()
	{
		// Are we server side and with a world?
		if( FMLCommonHandler.instance().getEffectiveSide().isClient() || ( this.worldObj == null ) )
		{
			// Nothing to do
			return;
		}

		// Get the colors of our neighbors
		AEColor[] sideColors = TileProviderBase.getNeighborCableColors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );

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
				this.setGridColor( sideColors[this.attachmentSide] );

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
					this.setGridColor( sideColors[index] );

					return;
				}
			}
		}

		// No cables match our color, set attachment to unknown
		this.attachmentSide = ForgeDirection.UNKNOWN.ordinal();

		// Set color to transparent
		this.setGridColor( AEColor.Transparent );

	}

	@MENetworkEventSubscribe
	public void channelEvent( MENetworkChannelsChanged event )
	{
		this.channelUpdated();
	}

	@MENetworkEventSubscribe
	public final void powerEvent( MENetworkPowerStatusChange event )
	{
		this.markForUpdate();
	}

	protected void channelUpdated()
	{
		// Check that our color is still valid
		this.checkGridConnectionColor();
	}

	// Returns how much was extracted
	protected int extractEssentiaFromNetwork( Aspect wantedAspect, int wantedAmount, boolean mustMatch )
	{
		// Ensure we have a monitor
		if( this.getFluidMonitor() )
		{
			// Get the gas version of the aspect
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( wantedAspect );

			IAEFluidStack request = EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, wantedAmount );

			// Simulate the extraction
			IAEFluidStack fluidStack = this.monitor.extractItems( request, Actionable.SIMULATE, new MachineSource( this ) );

			// Were we able to extract any?
			if( fluidStack == null )
			{
				return 0;
			}
			// Are we in match mode?
			else if( mustMatch )
			{
				// Does the amount match how much we want?
				if( fluidStack.getStackSize() != EssentiaConversionHelper.convertEssentiaAmountToFluidAmount( wantedAmount ) )
				{
					// Could not provide enough essentia
					return 0;
				}
			}

			// Take from the network
			this.monitor.extractItems( request, Actionable.MODULATE, new MachineSource( this ) );

			// Return how much was extracted
			return (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );
		}

		return 0;

	}

	protected AspectStack getAspectStackFromNetwork( Aspect searchAspect )
	{
		// Get the list from the network
		List<AspectStack> aspectStackList = this.getNetworkAspects();

		// Ensure we have a list, and that it is not empty
		if( ( aspectStackList != null ) && ( !aspectStackList.isEmpty() ) )
		{
			// Search all aspects in the list
			for( AspectStack currentStack : aspectStackList )
			{
				// Do the current match the search?
				if( currentStack.aspect == searchAspect )
				{
					// Found it
					return currentStack;
				}
			}
		}

		return null;
	}

	protected List<AspectStack> getNetworkAspects()
	{
		// Ensure we have a monitor
		if( this.getFluidMonitor() )
		{
			return EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );
		}

		return null;
	}

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

		// Set our monitor
		this.monitor = storageGrid.getFluidInventory();

		return( this.monitor != null );
	}

	private static AEColor[] getNeighborCableColors( IBlockAccess world, int x, int y, int z )
	{
		AEColor[] sideColors = new AEColor[6];

		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			// Get the tile entity on the current side
			TileEntity tileEntity = world.getTileEntity( x + side.offsetX, y + side.offsetY, z + side.offsetZ );

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

			// Causes more problems than its worth
			/*
			if( tileEntity instanceof AENetworkTile )
			{
				try
				{
					sideColors[side.ordinal()] = ( (AENetworkTile)tileEntity ).getGridNode( ForgeDirection.UNKNOWN ).getGridBlock().getGridColor();
				}
				catch( Throwable _ )
				{
				}
			}
			*/

		}

		return sideColors;
	}

	public boolean isActive()
	{
		// Are we server side?
		if( !this.getWorldObj().isRemote )
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

}
