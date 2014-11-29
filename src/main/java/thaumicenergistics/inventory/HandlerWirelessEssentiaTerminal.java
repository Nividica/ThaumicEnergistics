package thaumicenergistics.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
	/**
	 * Wireless terminal itemstack.
	 */
	private ItemStack wirelessTerminal;

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
	 * Item instance.
	 */
	private ItemWirelessEssentiaTerminal internalHandler;

	/**
	 * Player who is using the wireless terminal.
	 */
	private EntityPlayer player;

	/**
	 * Network source representing the player who is interacting with the
	 * container.
	 */
	private PlayerSource playerSource = null;

	public HandlerWirelessEssentiaTerminal( final EntityPlayer player, final ItemStack wirelessTerminal )
	{
		this.setPlayer( player );
		this.setTerminal( wirelessTerminal );
	}

	public HandlerWirelessEssentiaTerminal( final IWirelessAccessPoint accessPoint )
	{
		// Set the access point
		this.setAccessPoint( accessPoint );
	}

	/**
	 * Checks if the AP is still active and in range.
	 * 
	 * @return
	 */
	private boolean isAPInRangeAndActive()
	{
		return( ( this.accessPoint.isActive() ) && ( this.internalHandler.isAPInRangeOfPlayer( this.locationOfAccessPoint, this.rangeOfAccessPoint,
			this.player ) ) );
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
			return( this.internalHandler.getAECurrentPower( this.wirelessTerminal ) >= amount );
		}

		// Return true if enough power was extracted.
		return( this.internalHandler.extractAEPower( this.wirelessTerminal, amount ) == amount );
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
		return this.internalHandler.getSortingMode( this.wirelessTerminal );
	}

	/**
	 * Gets the wireless terminal itemstack.
	 * 
	 * @return
	 */
	public ItemStack getTerminal()
	{
		return this.wirelessTerminal;
	}

	/**
	 * Gets the power multiplier for wireless operations.
	 * 
	 * @return
	 */
	public double getWirelessPowerMultiplier()
	{
		// Get the squared distance
		double distance = this.internalHandler.getSquaredPlayerDistanceFromAP( this.locationOfAccessPoint, this.player );

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
		return( this.internalHandler.getAECurrentPower( this.wirelessTerminal ) > 0 );
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
				if( this.internalHandler.isAPInRangeOfPlayer( AP.getLocation(), AP.getRange(), this.player ) )
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
	 * Sets the player.
	 * 
	 * @param player
	 */
	public void setPlayer( final EntityPlayer player )
	{
		this.player = player;

		if( this.accessPoint != null )
		{
			// Create the action source
			this.playerSource = new PlayerSource( this.player, this.accessPoint );
		}
	}

	/**
	 * Sets the terminals sorting mode.
	 * 
	 * @param mode
	 */
	public void setSortingMode( final ComparatorMode mode )
	{
		this.internalHandler.setSortingMode( this.wirelessTerminal, mode );
	}

	public void setTerminal( final ItemStack wirelessTerminal )
	{
		// Set the terminal
		this.wirelessTerminal = wirelessTerminal;

		// Get and set the wireless-terminal-item handler
		this.internalHandler = (ItemWirelessEssentiaTerminal)wirelessTerminal.getItem();
	}

}
