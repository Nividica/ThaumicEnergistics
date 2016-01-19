package thaumicenergistics.common.inventory;

import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.api.IThEWirelessEssentiaTerminal;
import thaumicenergistics.api.ThEApi;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.api.grid.IMEEssentiaMonitor;
import thaumicenergistics.common.grid.EssentiaPassThroughMonitor;
import thaumicenergistics.common.grid.WirelessAELink;
import thaumicenergistics.common.items.ItemEnum;
import thaumicenergistics.common.items.ItemWirelessEssentiaTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.storage.AspectStackComparator.AspectStackComparatorMode;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;

public class HandlerWirelessEssentiaTerminal
	extends WirelessAELink
	implements IActionHost, ICraftingIssuerHost, IGuiItemObject
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
	 * The itemstack that represents this terminal.
	 */
	final private ItemStack wirelessItemstack;

	/**
	 * How much to multiply the required power by.
	 */
	private double wirelessPowerMultiplier = 1.0D;

	public HandlerWirelessEssentiaTerminal( final EntityPlayer player, final String encryptionKey,
											final IThEWirelessEssentiaTerminal wirelessTerminalInterface,
											final ItemStack wirelessTerminalItemstack )
	{
		// Call super
		super( player, encryptionKey );

		// Set the terminal interface
		this.wirelessTerminal = wirelessTerminalInterface;

		// Set the itemstack
		this.wirelessItemstack = wirelessTerminalItemstack;
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

	@Override
	protected int getUserPositionX()
	{
		return (int)this.player.posX;
	}

	@Override
	protected int getUserPositionY()
	{
		return (int)this.player.posY;
	}

	@Override
	protected int getUserPositionZ()
	{
		return (int)this.player.posZ;
	}

	@Override
	protected World getUserWorld()
	{
		return this.player.worldObj;
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
		return this.actionSource;
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
	@Override
	public IMEEssentiaMonitor getEssentiaInventory()
	{

		// Get the network essentia monitor
		IMEEssentiaMonitor essMonitor = super.getEssentiaInventory();
		if( essMonitor == null )
		{
			return null;
		}

		// Create the power redirector
		PowerRedirector pr = new PowerRedirector();

		// Create and return the passthrough essentia monitor
		IMEEssentiaMonitor monitor = new EssentiaPassThroughMonitor( essMonitor, pr );
		return monitor;
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
	@Override
	public boolean hasPowerToCommunicate()
	{
		return( this.wirelessTerminal.getAECurrentPower( this.wirelessItemstack ) > 0 );
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
		double distance = WirelessAELink.getSquaredDistanceFromAP( this.apLocation,
			this.getUserPositionX(),
			this.getUserPositionY(),
			this.getUserPositionZ() );

		// Calculate the distance
		distance = Math.sqrt( distance );

		// Calculate the power multiplier
		this.wirelessPowerMultiplier = AEConfig.instance.wireless_getDrainRate( distance );
	}

}
