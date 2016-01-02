package thaumicenergistics.inventory;

import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.api.IThEWirelessEssentiaTerminal;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.networking.IEssentiaGrid;
import thaumicenergistics.api.networking.IMEEssentiaMonitor;
import thaumicenergistics.api.storage.ICraftingIssuerHost;
import thaumicenergistics.aspect.AspectStackComparator.AspectStackComparatorMode;
import thaumicenergistics.grid.EssentiaPassThroughMonitor;
import thaumicenergistics.items.ItemWirelessEssentiaTerminal;
import thaumicenergistics.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.util.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.tile.networking.TileWireless;

public class HandlerWirelessEssentiaTerminal
	implements IActionHost, ICraftingIssuerHost, ITerminalHost, IGuiItemObject
{
	/**
	 * Redirects power requests to the wireless terminal.
	 * 
	 * @author Nividica
	 * 
	 */
	private class PowerRedirector
		implements IEnergyGrid
	{

		public PowerRedirector()
		{
		}

		@Override
		public void addNode( final IGridNode gridNode, final IGridHost machine )
		{
			// Ignored
		}

		@Override
		public double extractAEPower( final double amt, final Actionable mode, final PowerMultiplier usePowerMultiplier )
		{
			return HandlerWirelessEssentiaTerminal.this.extractPower( amt, mode );
		}

		@Override
		public double extractAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
		{
			return HandlerWirelessEssentiaTerminal.this.extractPower( amt, mode );
		}

		@Override
		public double getAvgPowerInjection()
		{
			// Ignored
			return 0;
		}

		@Override
		public double getAvgPowerUsage()
		{
			// Ignored
			return 0;
		}

		@Override
		public double getEnergyDemand( final double maxRequired )
		{
			// Ignored
			return 0;
		}

		@Override
		public double getEnergyDemand( final double d, final Set<IEnergyGrid> seen )
		{
			// Ignored
			return 0;
		}

		@Override
		public double getIdlePowerUsage()
		{
			// Ignored
			return 0;
		}

		@Override
		public double getMaxStoredPower()
		{
			// Ignored
			return 0;
		}

		@Override
		public double getStoredPower()
		{
			// Ignored
			return 0;
		}

		@Override
		public double injectAEPower( final double amt, final Actionable mode, final Set<IEnergyGrid> seen )
		{
			// Ignored
			return amt;
		}

		@Override
		public double injectPower( final double amt, final Actionable mode )
		{
			// Ignored
			return amt;
		}

		@Override
		public boolean isNetworkPowered()
		{
			// Ignored
			return true;
		}

		@Override
		public void onJoin( final IGridStorage sourceStorage )
		{
			// Ignored
		}

		@Override
		public void onSplit( final IGridStorage destinationStorage )
		{
			// Ignored
		}

		@Override
		public void onUpdateTick()
		{
			// Ignored
		}

		@Override
		public void populateGridStorage( final IGridStorage destinationStorage )
		{
			// Ignored
		}

		@Override
		public void removeNode( final IGridNode gridNode, final IGridHost machine )
		{
			// Ignored
		}

	}

	/**
	 * NBT Keys
	 */
	private static final String NBT_KEY_SORTING_MODE = "SortingMode2", NBT_KEY_VIEW_MODE = "ViewMode";

	/**
	 * Wireless terminal.
	 */
	private final IThEWirelessEssentiaTerminal wirelessTerminal;

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
	final private ItemStack wirelessItemstack;

	/**
	 * How much to multiply the required power by.
	 */
	private double wirelessPowerMultiplier = 1.0D;

	public HandlerWirelessEssentiaTerminal( final EntityPlayer player, final IWirelessAccessPoint accessPoint,
											final IThEWirelessEssentiaTerminal wirelessTerminalInterface,
											final ItemStack wirelessTerminalItemstack )
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
	public static boolean isTerminalLinked( final IThEWirelessEssentiaTerminal wirelessTerminal, final ItemStack wirelessTerminalItemstack )
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
		if( this.accessPoint != null )
		{
			return( ( this.accessPoint.isActive() ) && ( HandlerWirelessEssentiaTerminal.isAPInRangeOfPlayer( this.locationOfAccessPoint,
				this.rangeOfAccessPoint, this.player ) ) );
		}
		return false;
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
		if( accessPoint == null )
		{
			// Most likely on the client side.
			return;
		}

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
	 * Uses some of the terminals stored power. This does take into account the extra power required for wireless operations.
	 * 
	 * @param amount
	 * @return Returns the amount extracted.
	 */
	public double extractPower( final double amount, final Actionable mode )
	{
		// Has power usage been disabled?
		if( ItemWirelessEssentiaTerminal.GLOBAL_POWER_MULTIPLIER == 0 )
		{
			// Lie and say it was taken.
			return amount;
		}

		// Adjust by the power multiplier
		double adjustedAmount = amount * this.wirelessPowerMultiplier;

		double extractedAmount = 0;
		// Simulation?
		if( mode == Actionable.SIMULATE )
		{
			// Return the amount stored, capped at amount
			extractedAmount = Math.min( this.wirelessTerminal.getAECurrentPower( this.wirelessItemstack ), adjustedAmount );
		}
		else
		{
			// Return the amount extracted.
			extractedAmount = this.wirelessTerminal.extractAEPower( this.wirelessItemstack, adjustedAmount );
		}

		// Adjust by the power multiplier
		extractedAmount /= this.wirelessPowerMultiplier;

		return extractedAmount;
	}

	@Override
	public IGridNode getActionableNode()
	{
		if( this.accessPoint != null )
		{
			return this.accessPoint.getActionableNode();
		}
		return null;
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

	@Override
	public AECableType getCableConnectionType( final ForgeDirection dir )
	{
		return AECableType.NONE;
	}

	@Override
	public IConfigManager getConfigManager()
	{
		// Ignored.
		return null;
	}

	/**
	 * Gets the essentia monitor.
	 * 
	 * @return
	 */
	public IMEEssentiaMonitor getEssentiaMonitor()
	{
		if( this.accessPoint == null )
		{
			return null;
		}

		try
		{
			// Get the network essentia monitor
			IMEEssentiaMonitor essMonitor = ( (IMEEssentiaMonitor)this.accessPoint.getGrid().getCache( IEssentiaGrid.class ) );

			// Create the power redirector
			PowerRedirector pr = new PowerRedirector();

			// Create and return the essentia monitor
			IMEEssentiaMonitor monitor = new EssentiaPassThroughMonitor( essMonitor, pr );
			return monitor;
		}
		catch( Exception e )
		{
			return null;
		}
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		if( this.accessPoint == null )
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
	public IGridNode getGridNode( final ForgeDirection dir )
	{
		return this.getActionableNode();
	}

	@Override
	public ItemStack getIcon()
	{
		return ItemEnum.WIRELESS_TERMINAL.getStack();
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		if( this.accessPoint == null )
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

	@Override
	public ItemStack getItemStack()
	{
		return this.wirelessItemstack;
	}

	/**
	 * Gets the terminals sorting mode.
	 * 
	 * @return
	 */
	public AspectStackComparatorMode getSortingMode()
	{
		// Get the data tag
		NBTTagCompound dataTagCompound = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Does the tag have the mode stored?
		if( dataTagCompound.hasKey( NBT_KEY_SORTING_MODE ) )
		{
			return AspectStackComparatorMode.VALUES[dataTagCompound.getInteger( NBT_KEY_SORTING_MODE )];
		}

		return AspectStackComparatorMode.MODE_ALPHABETIC;
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

	public ViewItems getViewMode()
	{
		// Get the data tag
		NBTTagCompound dataTagCompound = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Does the tag have the mode stored?
		if( dataTagCompound.hasKey( NBT_KEY_VIEW_MODE ) )
		{
			return EnumCache.AE_VIEW_ITEMS[dataTagCompound.getInteger( NBT_KEY_VIEW_MODE )];
		}

		return ViewItems.ALL;

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
		if( this.accessPoint == null )
		{
			return false;
		}

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

	@Override
	public void launchGUI( final EntityPlayer player )
	{
		if( EffectiveSide.isClientSide() )
		{
			// Ask server to change GUI's
			Packet_S_ChangeGui.sendGuiChangeToWirelessTerminal( player );
		}
		else
		{
			// Open the gui
			ThEApi.instance().interact().openWirelessTerminalGui( player );
		}
	}

	@Override
	public void securityBreak()
	{
	}

	/**
	 * Sets the terminals sorting mode.
	 * 
	 * @param mode
	 */
	public void setSortingMode( final AspectStackComparatorMode mode )
	{
		// Get the data tag
		NBTTagCompound dataTag = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Set the sorting mode
		dataTag.setInteger( NBT_KEY_SORTING_MODE, mode.ordinal() );
	}

	public void setViewMode( final ViewItems viewMode )
	{
		// Get the data tag
		NBTTagCompound dataTag = this.wirelessTerminal.getWETerminalTag( this.wirelessItemstack );

		// Set the viewing mode
		dataTag.setInteger( NBT_KEY_VIEW_MODE, viewMode.ordinal() );
	}

	/**
	 * Updates the power multiplier for wireless operations.
	 * 
	 * @return
	 */
	public void updatePowerMultiplier()
	{
		// Get the squared distance
		double distance = HandlerWirelessEssentiaTerminal.getSquaredPlayerDistanceFromAP( this.locationOfAccessPoint, this.player );

		// Calculate the distance
		distance = Math.sqrt( distance );

		// Calculate the power multiplier
		this.wirelessPowerMultiplier = AEConfig.instance.wireless_getDrainRate( distance );
	}

}
