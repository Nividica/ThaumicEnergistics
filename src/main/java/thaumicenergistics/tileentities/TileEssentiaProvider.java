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
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.util.EssentiaConversionHelper;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.me.GridAccessException;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.networking.TileCableBus;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEssentiaProvider
	extends AENetworkTile
	implements IEssentiaTransport, IAEPowerStorage
{
	public static final String TILE_ID = "TileEssentiaProvider";
	private static final String NBTKEY_COLOR = "TEColor";
	private static final String NBTKEY_ATTACHMENT = "TEAttachSide";

	protected int attachmentSide;

	private AETileEventHandler eventHandler = new AETileEventHandler( TileEventType.WORLD_NBT, TileEventType.NETWORK )
	{
		@Override
		public void writeToNBT( NBTTagCompound data )
		{
			// Write our color to the tag
			data.setInteger( TileEssentiaProvider.NBTKEY_COLOR, TileEssentiaProvider.this.getGridColor().ordinal() );

			// Write the attachment side to the tag
			data.setInteger( TileEssentiaProvider.NBTKEY_ATTACHMENT, TileEssentiaProvider.this.attachmentSide );
		}

		@Override
		public void readFromNBT( NBTTagCompound data )
		{
			// Do we have the color key?
			if ( data.hasKey( TileEssentiaProvider.NBTKEY_COLOR ) )
			{
				// Read the color from the tag
				TileEssentiaProvider.this.setGridColor( AEColor.values()[data.getInteger( TileEssentiaProvider.NBTKEY_COLOR )] );
			}

			// Do we have the attachment key?
			if ( data.hasKey( TileEssentiaProvider.NBTKEY_ATTACHMENT ) )
			{
				// Read the color from the tag
				TileEssentiaProvider.this.attachmentSide = data.getInteger( TileEssentiaProvider.NBTKEY_ATTACHMENT );
			}
		}

		@Override
		public void writeToStream( ByteBuf data ) throws IOException
		{
			// Write the color data to the stream
			data.writeInt( TileEssentiaProvider.this.getGridColor().ordinal() );
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean readFromStream( ByteBuf data ) throws IOException
		{
			// Read the color from the stream
			TileEssentiaProvider.this.setGridColor( AEColor.values()[data.readInt()] );
			return true;
		}
	};

	public IMEMonitor<IAEFluidStack> monitor = null;

	public TileEssentiaProvider()
	{
		this( ForgeDirection.UNKNOWN.ordinal() );
	}
	
	public TileEssentiaProvider( int attachmentSide )
	{
		// Register our event handler
		this.addNewHandler( this.eventHandler );

		// Ignored on client side
		if ( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Create the grid block
			this.createProxy();

			// Set that we require a channel
			this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		}

		// Set which side we are attached to
		this.attachmentSide = attachmentSide;
	}

	@Override
	public boolean isConnectable( ForgeDirection face )
	{
		// Can connect on any side
		return true;
	}

	@Override
	public boolean canInputFrom( ForgeDirection face )
	{
		// Doesn't accept essentia
		return false;
	}

	@Override
	public boolean canOutputTo( ForgeDirection face )
	{
		// Can output to any side
		return true;
	}

	@Override
	public void setSuction( Aspect aspect, int amount )
	{
		// Ignored
	}

	@Override
	public Aspect getSuctionType( ForgeDirection face )
	{
		// Doesn't accept essentia
		return null;
	}

	@Override
	public int getSuctionAmount( ForgeDirection face )
	{
		// Doesn't accept essentia
		return 0;
	}

	@Override
	public int takeEssentia( Aspect aspect, int amount, ForgeDirection face )
	{
		// Extract essentia from the network, and return the amount extracted
		return this.extractEssentiaFromNetwork( aspect, amount );
	}

	@Override
	public int addEssentia( Aspect aspect, int amount, ForgeDirection face )
	{
		// Doesn't accept essentia
		return 0;
	}

	@Override
	public Aspect getEssentiaType( ForgeDirection face )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( face );

		// Does the neighbor want anything?
		if ( wantedAspect != null )
		{
			// Does the network have that aspect?
			if ( this.getAspectStackFromNetwork( wantedAspect ) != null )
			{
				// Return the aspect they want
				return wantedAspect;
			}
		}

		// No match or no request
		return null;
	}

	@Override
	public int getEssentiaAmount( ForgeDirection face )
	{
		// Get the aspect this neighbor wants
		Aspect wantedAspect = this.getNeighborWantedAspect( face );

		// Does the neighbor want anything?
		if ( wantedAspect != null )
		{
			// Get the stack from the network
			AspectStack matchingStack = this.getAspectStackFromNetwork( wantedAspect );

			// Does the network have that aspect?
			if ( matchingStack != null )
			{
				// Return the amount we have
				return (int)matchingStack.amount;
			}
		}

		// No match or no request
		return 0;
	}

	@Override
	public int getMinimumSuction()
	{
		// Any amount of suction is good enough
		return 0;
	}

	@Override
	public boolean renderExtendedTube()
	{
		// As of now we take up a full block
		return false;
	}

	protected Aspect getNeighborWantedAspect( ForgeDirection face )
	{
		// Get the tile entity next to this face
		TileEntity neighbor = this.worldObj.getTileEntity( this.xCoord + face.offsetX, this.yCoord + face.offsetY, this.zCoord + face.offsetZ );

		// Do we have essentia transport neighbor?
		if ( ( neighbor != null ) && ( neighbor instanceof IEssentiaTransport ) )
		{
			// Get the aspect they want
			Aspect wantedAspect = ( (IEssentiaTransport)neighbor ).getSuctionType( face.getOpposite() );

			// Return the aspect they want
			return wantedAspect;
		}

		return null;
	}

	// Returns how much was extracted
	protected int extractEssentiaFromNetwork( Aspect wantedAspect, int wantedAmount )
	{
		// Ensure we have a monitor
		if ( this.hasFluidMonitor() )
		{
			// Get the gas version of the aspect
			GaseousEssentia essentiaGas = GaseousEssentia.getGasFromAspect( wantedAspect );

			// Take from the network
			IAEFluidStack fluidStack = this.monitor.extractItems(
				EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, wantedAmount ), Actionable.MODULATE,
				new MachineSource( this ) );

			// Were we able to extract any?
			if ( fluidStack != null )
			{
				// Return how much was extracted
				return (int)EssentiaConversionHelper.convertFluidAmountToEssentiaAmount( fluidStack.getStackSize() );
			}
		}

		return 0;

	}

	protected AspectStack getAspectStackFromNetwork( Aspect searchAspect )
	{
		// Get the list from the network
		List<AspectStack> aspectStackList = this.getNetworkAspects();

		// Ensure we have a list, and that it is not empty
		if ( ( aspectStackList != null ) && ( !aspectStackList.isEmpty() ) )
		{
			// Search all aspects in the list
			for( AspectStack currentStack : aspectStackList )
			{
				// Do the current match the search?
				if ( currentStack.aspect == searchAspect )
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
		if ( this.hasFluidMonitor() )
		{
			return EssentiaConversionHelper.convertIIAEFluidStackListToAspectStackList( this.monitor.getStorageList() );
		}

		return null;
	}

	protected boolean hasFluidMonitor()
	{
		// Get the grid node
		IGridNode node = this.gridProxy.getNode();

		// Ensure we have the node
		if ( node == null )
		{
			return false;
		}

		// Get the grid that node is connected to
		IGrid grid = node.getGrid();

		// Is there a grid?
		if ( grid == null )
		{
			return false;
		}

		// Access the storage grid
		IStorageGrid storageGrid = (IStorageGrid)grid.getCache( IStorageGrid.class );

		// Set our monitor
		this.monitor = storageGrid.getFluidInventory();

		return ( this.monitor != null );
	}

	@MENetworkEventSubscribe
	protected void PowerEvent( MENetworkPowerStorage.PowerEventType event )
	{
		// Is this a request power event?
		if ( event == MENetworkPowerStorage.PowerEventType.REQUEST_POWER )
		{
			// Ask the network for power
			try
			{
				this.gridProxy.getGrid().postEvent( new MENetworkPowerStorage( this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER ) );
			}
			catch( GridAccessException e )
			{
			}
		}
	}

	@MENetworkEventSubscribe
	public void ChannelEvent( MENetworkChannelsChanged event )
	{
		// Check that our color is still valid
		this.checkGridConnectionColor();
	}

	@Override
	public double extractAEPower( double amt, Actionable mode, PowerMultiplier usePowerMultiplier )
	{
		// TODO Figure out how much power to extract based on if we have done work
		return usePowerMultiplier.multiply( 2.0D );
	}

	@Override
	public double injectAEPower( double amt, Actionable mode )
	{
		// We do not generate power
		return 0;
	}

	@Override
	public double getAEMaxPower()
	{
		// We don't store power.
		return 0;
	}

	@Override
	public double getAECurrentPower()
	{
		// We don't store power
		return 0;
	}

	@Override
	public boolean isAEPublicPowerStorage()
	{
		// We don't store power
		return false;
	}

	@Override
	public AccessRestriction getPowerFlow()
	{
		// We only take power
		return AccessRestriction.READ;
	}

	@Override
	protected ItemStack getItemFromTile( Object obj )
	{
		// Return the itemstack the visually represents this tile
		return new ItemStack( BlockEnum.ESSENTIA_PROVIDER.getBlock(), 1 );

	}

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
		if ( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Get the grid node
			IGridNode gridNode = this.gridProxy.getNode();

			// Do we have a grid node?
			if ( gridNode != null )
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
		// Are we client side?
		if ( FMLCommonHandler.instance().getEffectiveSide().isClient() )
		{
			// Nothing to do on client side
			return;
		}
		
		// Get our current color
		AEColor currentColor = this.gridProxy.myColor;

		// Get the colors of our neighbors
		AEColor[] sideColors = TileEssentiaProvider.getNeighborColors( this.worldObj, this.xCoord, this.yCoord, this.zCoord );

		// Are we attached to a side?
		if ( this.attachmentSide != ForgeDirection.UNKNOWN.ordinal() )
		{
			// Does our attached side still exist
			if ( sideColors[this.attachmentSide] != null )
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

	private static AEColor[] getNeighborColors( IBlockAccess world, int x, int y, int z )
	{
		AEColor[] sideColors = new AEColor[6];

		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			// Get the tile entity on the current side
			TileEntity tileEntity = world.getTileEntity( x + side.offsetX, y + side.offsetY, z + side.offsetZ );

			// Did we get an entity?
			if ( tileEntity == null )
			{
				continue;
			}

			// Is that entity a cable?
			if ( !( tileEntity instanceof TileCableBus ) )
			{
				continue;
			}

			// Set the color
			sideColors[side.ordinal()] = ( (TileCableBus)tileEntity ).getColor();
		}

		return sideColors;
	}

}
