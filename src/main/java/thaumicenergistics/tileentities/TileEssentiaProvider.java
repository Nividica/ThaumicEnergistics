package thaumicenergistics.tileentities;

import java.util.List;
import cpw.mods.fml.common.FMLCommonHandler;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.me.GridAccessException;
import appeng.tile.grid.AENetworkTile;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.fluids.GaseousEssentia;
import thaumicenergistics.registries.BlockEnum;
import thaumicenergistics.util.EssentiaConversionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

//TODO: Client-Server sync on colors, save to NBT tag
public class TileEssentiaProvider
	extends AENetworkTile
	implements IEssentiaTransport, IAEPowerStorage
{
	public static final String TileID = "TileEssentiaProvider";

	public IMEMonitor<IAEFluidStack> monitor = null;

	public TileEssentiaProvider()
	{
		// Ignored on client side
		if ( FMLCommonHandler.instance().getEffectiveSide().isServer() )
		{
			// Create the grid block
			this.createProxy();
			
			// Set that we require a channel
			this.gridProxy.setFlags( GridFlags.REQUIRE_CHANNEL );
		}
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
		System.out.println( "takeEssentia");
		
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
		System.out.println( "getEssentiaType");
		
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
			IAEFluidStack fluidStack = this.monitor.extractItems( EssentiaConversionHelper.createAEFluidStackInEssentiaUnits( essentiaGas, wantedAmount ), Actionable.MODULATE,
				new MachineSource( this ) );
			
			// Were we able to extract any?
			if( fluidStack != null )
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
	
	public void onNeighborChange( IBlockAccess world, int x, int y, int z )
	{
		// Evaluate the state of our neighbors
		AEColor neighborhoodColor = TileEssentiaProvider.getNeighborColors( world, x, y, z );
		
		// Do we have any neighbors with color?
		if( neighborhoodColor == null )
		{
			// Make us transparent
			this.gridProxy.myColor = AEColor.Transparent;
			return;
		}
		
		// Are we transparent?
		if( this.gridProxy.myColor == AEColor.Transparent )
		{
			// We can accept the neighbor color
			this.gridProxy.myColor = neighborhoodColor;
			
			// Mark for an update
			this.markForUpdate();
		}
	}
	
	private static AEColor getNeighborColors( IBlockAccess world, int x, int y, int z )
	{	
		for( ForgeDirection side : ForgeDirection.VALID_DIRECTIONS )
		{
			// Does the neighbor on this side have a color?
			AEColor nColor = TileEssentiaProvider.getNeighborColor( world, x, y, z, side );
			
			if( nColor != null )
			{
				// Found something with color, return the color
				return nColor;
			}
		}
		
		return null;
	}
	
	private static AEColor getNeighborColor( IBlockAccess world, int x, int y, int z, ForgeDirection side )
	{
		// Get the tile entity on that side
		TileEntity tileEntity = world.getTileEntity( x + side.offsetX, y + side.offsetY, z + side.offsetZ );
		
		// Did we get an entity?
		if( tileEntity == null )
		{
			return null;
		}
		
		// Is that entity a grid host?
		if( !( tileEntity instanceof IGridHost ) )
		{
			return null;
		}
		
		// Get the grid node
		IGridNode node = ( (IGridHost)tileEntity ).getGridNode( ForgeDirection.UNKNOWN );
		
		// Does the host have a node?
		if( node == null )
		{
			return null;
		}
		
		// Get the grid block for the node
		IGridBlock grid = node.getGridBlock();
		
		// Did we get a grid?
		if( grid == null )
		{
			return null;
		}
		
		// Get the color of 
		AEColor neighborColor = grid.getGridColor();
		
		// Return the color
		return neighborColor;
	}

}
