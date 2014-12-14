package thaumicenergistics.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumicenergistics.api.IWirelessEssentiaTerminal;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.items.ItemWirelessEssentiaTerminal;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.tile.networking.TileWireless;

public class HandlerWirelessEssentiaTerminal
{
	private static final String NBT_KEY_SORTING_MODE = "SortingMode";

	/**
	 * Wireless terminal.
	 */
	private IWirelessEssentiaTerminal wirelessTerminal;

	/**
	 * Access point used to communicate with the AE network.
	 */
	private IWirelessAccessPoint accessPoint;

	/**
	 * Cached value of the range of the access point, squared.
	 */
	private double rangeOfAccessPoint;

	/**
	 * Where in the world is the access point.
	 */
	private DimensionalCoord locationOfAccessPoint;

	/**
	 * Player who is using the wireless terminal.
	 */
	private EntityPlayer player;

	/**
	 * Network source representing the player who is interacting with the
	 * container.
	 */
	private PlayerSource playerSource = null;

	/**
	 * The itemstack that represents this terminal.
	 */
	private ItemStack wirelessItemstack;

	public HandlerWirelessEssentiaTerminal( final EntityPlayer player, final IWirelessAccessPoint accessPoint,
											final IWirelessEssentiaTerminal wirelessTerminalInterface, final ItemStack wirelessTerminalItemstack )
	{
		// Set the player
		this.player = player;

		// Set the terminal interface
		this.wirelessTerminal = wirelessTerminalInterface;

		// Set the itemstack
		this.wirelessItemstack = wirelessTerminalItemstack;

		// Set the access point
		this.setAccessPoint( accessPoint );
	}

	/**
	 * Gets the distance the specified player is from the AP.
	 * 
	 * @param APLocation
	 * @param player
	 * @return
	 */
	private static double getSquaredPlayerDistanceFromAP( final DimensionalCoord APLocation, final EntityPlayer player )
	{
		// Get the player position
		int pX = (int)Math.floor( player.posX ), pY = (int)Math.floor( player.posY ), pZ = (int)Math.floor( player.posZ );

		// Calculate the distance from the AP
		int dX = APLocation.x - pX, dY = APLocation.y - pY, dZ = APLocation.z - pZ;

		// Calculate the square distance
		int squareDistance = ( dX * dX ) + ( dY * dY ) + ( dZ * dZ );

		return squareDistance;
	}

	/**
	 * Checks if the AP at the specified location and has the specified range,
	 * is close enough to communicate with.
	 * 
	 * @param APLocation
	 * @param APRange
	 * @param X
	 * @param Y
	 * @param Z
	 * @return
	 */
	public static boolean isAPInRangeOfPlayer( final DimensionalCoord APLocation, final double APRange, final EntityPlayer player )
	{
		// Is the AP and the player in the same world?
		if( !APLocation.isInWorld( player.worldObj ) )
		{
			return false;
		}

		// Calculate the square distance
		double squareDistance = HandlerWirelessEssentiaTerminal.getSquaredPlayerDistanceFromAP( APLocation, player );

		// Return if close enough to use AP
		return squareDistance <= ( APRange * APRange );
	}

	/**
	 * Returns true if the wireless terminal is linked to a network.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	public static boolean isTerminalLinked( final IWirelessEssentiaTerminal wirelessTerminal, final ItemStack wirelessTerminalItemstack )
	{
		return( !wirelessTerminal.getEncryptionKey( wirelessTerminalItemstack ).isEmpty() );
	}

	/**
	 * Checks if the AP is still active and in range.
	 * 
	 * @return
	 */
	private boolean isAPInRangeAndActive()
	{
		return( ( this.accessPoint.isActive() ) && ( HandlerWirelessEssentiaTerminal.isAPInRangeOfPlayer( this.locationOfAccessPoint,
			this.rangeOfAccessPoint, this.player ) ) );
	}

	/**
	 * Set's the access point used for communication.
	 * 
	 * @param accessPoint
	 */
	private void setAccessPoint( final IWirelessAccessPoint accessPoint )
	{
		// Set the access point
		this.accessPoint = accessPoint;

		// Get the range of the access point
		this.rangeOfAccessPoint = this.accessPoint.getRange();

		// Get the location of the access point
		this.locationOfAccessPoint = this.accessPoint.getLocation();

		// Create the action source
		if( this.player != null )
		{
			this.playerSource = new PlayerSource( this.player, this.accessPoint );
		}
	}

	/**
	 * Uses some of the terminals stored power. This does not take into account
	 * the
	 * extra power required for wireless operations.
	 * 
	 * @param amount
	 * @return Returns true if the full power was/can be extracted, false
	 * otherwise.
	 */
	public boolean extractPower( final double amount, final Actionable mode )
	{
		// Has power usage been disabled?
		if( ItemWirelessEssentiaTerminal.GLOBAL_POWER_MULTIPLIER == 0 )
		{
			// Lie and say it was taken.
			return true;
		}

		// Simulation?
		if( mode == Actionable.SIMULATE )
		{
			// Return true if there is enough power to satisfy the request.
			return( this.wirelessTerminal.getAECurrentPower( this.wirelessItemstack ) >= amount );
		}

		// Return true if enough power was extracted.
		return( this.wirelessTerminal.extractAEPower( this.wirelessItemstack, amount ) == amount );
	}

	/**
	 * Gets the the player source used when interacting with the network.
	 * 
	 * @return
	 */
	public BaseActionSource getActionHost()
	{
		return this.playerSource;
	}

	/**
	 * Gets the fluid monitor.
	 * 
	 * @return
	 */
	public IMEMonitor<IAEFluidStack> getMonitor()
	{
		try
		{
			return ( (IStorageGrid)this.accessPoint.getGrid().getCache( IStorageGrid.class ) ).getFluidInventory();
		}
		catch( Exception e )
		{
			return null;
		}
	}

	/**
	 * Gets the terminals sorting mode.
	 * 
	 * @return
	 */
	public ComparatorMode getSortingMode()
	{
		// Get the data tag
		NBTTagCompound dataTagCompound = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Does the tag have the sorting mode stored?
		if( dataTagCompound.hasKey( HandlerWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE ) )
		{
			return ComparatorMode.valueOf( dataTagCompound.getString( HandlerWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE ) );
		}

		return ComparatorMode.MODE_ALPHABETIC;
	}

	/**
	 * Gets the wireless terminal itemstack.
	 * 
	 * @return
	 */
	public ItemStack getTerminalItem()
	{
		return this.wirelessItemstack;
	}

	/**
	 * Gets the power multiplier for wireless operations.
	 * 
	 * @return
	 */
	public double getWirelessPowerMultiplier()
	{
		// Get the squared distance
		double distance = HandlerWirelessEssentiaTerminal.getSquaredPlayerDistanceFromAP( this.locationOfAccessPoint, this.player );

		// Calculate the distance
		distance = Math.sqrt( distance );

		// Calculate the power multiplier
		return AEConfig.instance.wireless_getDrainRate( distance );
	}

	/**
	 * Returns true if the terminal has power.
	 * 
	 * @return
	 */
	public boolean hasPower()
	{
		return( this.wirelessTerminal.getAECurrentPower( this.wirelessItemstack ) > 0 );
	}

	/**
	 * Checks if the terminal is connected to the network.
	 * 
	 * @return True if the terminal is connected, false otherwise.
	 */
	public boolean isConnected()
	{
		// Does the terminal have power?
		if( !this.hasPower() )
		{
			// Terminal is dead.
			return false;
		}

		// Is the current AP still good?
		if( this.isAPInRangeAndActive() )
		{
			// Current AP is still connected.
			return true;
		}

		// Get all AP's on the grid
		IMachineSet accessPoints = this.accessPoint.getGrid().getMachines( TileWireless.class );

		// Loop over AP's and see if any are close enough to communicate with
		for( IGridNode APNode : accessPoints )
		{
			// Get the AP
			IWirelessAccessPoint AP = (IWirelessAccessPoint)APNode.getMachine();

			// Skip if current AP
			if( AP.equals( this.accessPoint ) )
			{
				continue;
			}

			// Is the AP active?
			if( AP.isActive() )
			{
				// Is the player close enough to the AP?
				if( HandlerWirelessEssentiaTerminal.isAPInRangeOfPlayer( AP.getLocation(), AP.getRange(), this.player ) )
				{
					// Set the new AP
					this.setAccessPoint( AP );

					// Found usable AP
					return true;
				}
			}
		}

		// No AP's in range.
		return false;
	}

	/**
	 * Sets the terminals sorting mode.
	 * 
	 * @param mode
	 */
	public void setSortingMode( final ComparatorMode mode )
	{
		// Get the data tag
		NBTTagCompound dataTag = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Set the sorting mode
		dataTag.setString( HandlerWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE, mode.name() );
	}

}
