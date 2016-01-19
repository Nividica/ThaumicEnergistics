package thaumicenergistics.common.grid;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.api.grid.IEssentiaGrid;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;

public abstract class WirelessAELink
	implements IStorageMonitorable
{
	/**
	 * The player who owns this link.
	 */
	protected final EntityPlayer player;

	/**
	 * Encryption key used to access the network.
	 */
	protected final String encryptionKey;

	/**
	 * Access point used to communicate with the AE network.
	 */
	protected IWirelessAccessPoint accessPoint;

	/**
	 * Where in the world is the access point.
	 */
	protected DimensionalCoord apLocation = null;

	/**
	 * Network source representing the player
	 */
	protected BaseActionSource actionSource;

	public WirelessAELink( final @Nullable EntityPlayer player, @Nonnull final String encryptionKey )
	{
		// Set the player
		this.player = player;

		// Set the enc key
		this.encryptionKey = encryptionKey;

		// Link with the AP
		this.linkWithNewAP();
	}

	/**
	 * Checks if the AP at the specified location and has the specified range,
	 * is close enough to communicate with.
	 * 
	 * @param APLocation
	 * @param APRange
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static boolean isAPInRange( final DimensionalCoord APLocation, final double APRange, final World world, final int x, final int y,
										final int z )
	{
		// Is the AP in the same world?
		if( !APLocation.isInWorld( world ) )
		{
			return false;
		}

		// Calculate the square distance
		double squareDistance = getSquaredDistanceFromAP( APLocation, x, y, z );

		// Return if close enough to use AP
		return squareDistance <= ( APRange * APRange );
	}

	/**
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param grid
	 * @return
	 */
	private static ArrayList<IWirelessAccessPoint> locateAPsInRange( final World world, final int x, final int y, final int z, final IGrid grid )
	{
		// Get all AP's on the grid
		IMachineSet accessPoints = grid.getMachines( TileWireless.class );
		if( accessPoints.isEmpty() )
		{
			return null;
		}

		// Create the list
		ArrayList<IWirelessAccessPoint> aps = new ArrayList<IWirelessAccessPoint>();

		// Loop over AP's and see if any are close enough to communicate with
		for( IGridNode APNode : accessPoints )
		{
			// Get the AP
			IWirelessAccessPoint AP = (IWirelessAccessPoint)APNode.getMachine();

			// Is the AP active?
			if( AP.isActive() )
			{
				// Close enough to the AP?
				if( isAPInRange( AP.getLocation(), AP.getRange(), world, x, y, z ) )
				{
					aps.add( AP );
				}
			}
		}

		return aps;
	}

	/**
	 * Returns the squared distance the specified coords are from the Access Point.
	 * 
	 * @param locationAP
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	protected static double getSquaredDistanceFromAP( final DimensionalCoord locationAP, final int x, final int y, final int z )
	{
		if( locationAP == null )
		{
			return Double.MAX_VALUE;
		}

		// Calculate the distance from the AP
		int dX = locationAP.x - x, dY = locationAP.y - y, dZ = locationAP.z - z;

		// Calculate the square distance
		return( ( dX * dX ) + ( dY * dY ) + ( dZ * dZ ) );
	}

	/**
	 * Returns an unsorted list of AP's in range.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param encryptionKey
	 * @return Null on error, list of AP's in range otherwise.
	 */
	public static ArrayList<IWirelessAccessPoint> locateAPsInRange( final World world, final int x, final int y, final int z,
																	final String encryptionKey )
	{
		// Get the encryption key
		long encryptionValue;
		try
		{
			encryptionValue = Long.parseLong( encryptionKey );
		}
		catch( NumberFormatException e )
		{
			// Invalid security key
			return null;
		}

		// Get the linked source
		Object source = AEApi.instance().registries().locatable().getLocatableBy( encryptionValue );

		// Ensure it is a security terminal
		if( !( source instanceof TileSecurity ) )
		{
			// Invalid security terminal
			return null;
		}

		// Get the terminal
		TileSecurity securityHost = (TileSecurity)source;

		// Get the grid
		IGrid grid;
		try
		{
			grid = securityHost.getGridNode( ForgeDirection.UNKNOWN ).getGrid();
		}
		catch( Exception e )
		{
			// Can not find the grid
			return null;
		}

		return locateAPsInRange( world, x, y, z, grid );
	}

	/**
	 * Returns an unsorted list of AP's in range.
	 * 
	 * @param player
	 * @param encryptionKey
	 * @return Null on error, list of AP's in range otherwise.
	 */
	public static ArrayList<IWirelessAccessPoint> locateAPsInRangeOfPlayer( final EntityPlayer player, final String encryptionKey )
	{
		return locateAPsInRange( player.worldObj,
			(int)Math.floor( player.posX ),
			(int)Math.floor( player.posY ),
			(int)Math.floor( player.posZ ), encryptionKey );
	}

	/**
	 * Checks if the AP is still active and in range.
	 * 
	 * @return
	 */
	private boolean isAPInRangeAndActive()
	{
		// Has AP?
		if( this.accessPoint != null )
		{
			// Is active?
			if( this.accessPoint.isActive() )
			{
				// In range?
				return isAPInRange( this.apLocation, this.accessPoint.getRange(), this.getUserWorld(),
					this.getUserPositionX(), this.getUserPositionY(), this.getUserPositionZ() );
			}
		}
		return false;
	}

	/**
	 * Locates the closest AP in range and links with it.
	 * 
	 * @return True if AP linked with.
	 */
	private boolean linkWithNewAP()
	{
		World w = this.getUserWorld();
		int x = this.getUserPositionX();
		int y = this.getUserPositionY();
		int z = this.getUserPositionZ();

		IGrid grid = null;
		if( this.accessPoint != null )
		{
			try
			{
				grid = this.accessPoint.getGrid();
			}
			catch( Exception e )
			{
			}
		}

		// Get a list of AP's in range
		ArrayList<IWirelessAccessPoint> apList;
		if( grid != null )
		{
			apList = locateAPsInRange( w, x, y, z, grid );
		}
		else
		{
			apList = locateAPsInRange( w, x, y, z, this.encryptionKey );
		}

		if( ( apList == null ) || ( apList.isEmpty() ) )
		{
			// No valid AP's found
			this.accessPoint = null;
			return false;
		}

		// Get the closest
		IWirelessAccessPoint closest = null;
		double closestDistance = Double.MAX_VALUE;
		for( IWirelessAccessPoint ap : apList )
		{
			double dist = getSquaredDistanceFromAP( ap.getLocation(), x, y, z );
			if( dist < closestDistance )
			{
				closestDistance = dist;
				closest = ap;
			}
		}

		// Set the closest as the AP to use
		this.setAP( closest );

		return true;
	}

	/**
	 * Set's the access point used for communication.
	 * 
	 * @param accessPoint
	 */
	private void setAP( final IWirelessAccessPoint accessPoint )
	{
		// Set the access point
		this.accessPoint = accessPoint;

		// Get the location of the access point
		this.apLocation = this.accessPoint.getLocation();

		// Create the action source
		if( this.player != null )
		{
			this.actionSource = new PlayerSource( this.player, this.accessPoint );
		}
		else
		{
			this.actionSource = new MachineSource( this.accessPoint );
		}
	}

	/**
	 * Return the x position of the user.
	 * 
	 * @return
	 */
	protected abstract int getUserPositionX();

	/**
	 * Return the y position of the user.
	 * 
	 * @return
	 */
	protected abstract int getUserPositionY();

	/**
	 * Return the z position of the user.
	 * 
	 * @return
	 */
	protected abstract int getUserPositionZ();

	/**
	 * Return the world the user is in.
	 * 
	 * @return
	 */
	protected abstract World getUserWorld();

	/**
	 * Is there enough local power to communicate with the AP?
	 * 
	 * @return
	 */
	protected abstract boolean hasPowerToCommunicate();

	public IEnergyGrid getEnergyGrid()
	{
		// Check AP
		if( this.accessPoint == null )
		{
			return null;
		}

		try
		{
			// Get the energy grid
			return this.accessPoint.getActionableNode().getGrid().getCache( IEnergyGrid.class );
		}
		catch( Exception e )
		{
			// Ignored
		}

		return null;
	}

	/**
	 * Gets the essentia inventory.
	 * 
	 * @return
	 */
	public IMEEssentiaMonitor getEssentiaInventory()
	{
		// Check connectivity
		if( ( this.accessPoint == null ) || !this.isConnected() )
		{
			return null;
		}

		try
		{
			// Get the network essentia monitor
			return( (IMEEssentiaMonitor)this.accessPoint.getGrid().getCache( IEssentiaGrid.class ) );
		}
		catch( Exception e )
		{
			return null;
		}
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		// Check connectivity
		if( ( this.accessPoint == null ) || !this.isConnected() )
		{
			return null;
		}

		try
		{
			// Get the storage grid
			IStorageGrid storageGrid = this.accessPoint.getActionableNode().getGrid().getCache( IStorageGrid.class );

			// Return the monitor
			return storageGrid.getFluidInventory();
		}
		catch( Exception e )
		{
			// Ignored
		}

		return null;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		// Check connectivity
		if( ( this.accessPoint == null ) || !this.isConnected() )
		{
			return null;
		}

		try
		{
			// Get the storage grid
			IStorageGrid storageGrid = this.accessPoint.getActionableNode().getGrid().getCache( IStorageGrid.class );

			// Return the monitor
			return storageGrid.getItemInventory();
		}
		catch( Exception e )
		{
			// Ignored
		}

		return null;
	}

	/**
	 * Checks if the AP is still connected.
	 * 
	 * @return
	 */
	public boolean isConnected()
	{
		// Is there power?
		if( !this.hasPowerToCommunicate() )
		{
			return false;
		}

		// Is the current AP still good?
		if( this.isAPInRangeAndActive() )
		{
			// Current AP is still connected.
			return true;
		}

		// Attempt to link with a new AP
		return this.linkWithNewAP();
	}
}
