package thaumicenergistics.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStackComparator.ComparatorMode;
import thaumicenergistics.gui.TEGuiHandler;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.registries.ItemEnum;
import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.util.DimensionalCoord;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;

public class ItemWirelessEssentiaTerminal
	extends Item
	implements INetworkEncodable, IAEItemPowerStorage
{
	/**
	 * NBT keys
	 */
	private static final String NBT_AE_SOURCE_KEY = "SourceKey", NBT_STORED_POWER = "StoredPower", NBT_KEY_SORTING_MODE = "SortingMode";

	/**
	 * Amount of power the wireless terminal can store.
	 */
	private static final int POWER_STORAGE = 1600000;

	/**
	 * Used during power calculations.
	 */
	public static double GLOBAL_POWER_MULTIPLIER = PowerMultiplier.CONFIG.multiplier;

	/**
	 * Creates the wireless terminal item.
	 */
	public ItemWirelessEssentiaTerminal()
	{
		// Can not stack
		this.setMaxStackSize( 1 );

		this.setMaxDamage( 32 );

		this.hasSubtypes = false;
	}

	/**
	 * Gets or creates the NBT compound tag for the terminal.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	private NBTTagCompound getOrCreateCompoundTag( final ItemStack wirelessTerminal )
	{
		NBTTagCompound dataTag;

		// Ensure the terminal has a tag
		if( !wirelessTerminal.hasTagCompound() )
		{
			// Create a new tag.
			wirelessTerminal.setTagCompound( ( dataTag = new NBTTagCompound() ) );
		}
		else
		{
			// Get the tag
			dataTag = wirelessTerminal.getTagCompound();
		}

		return dataTag;
	}

	/**
	 * Opens the wireless gui for the specified player.
	 * The player must be holding a wireless terminal.
	 * 
	 * @param world
	 * @param player
	 */
	private void openWirelessTerminalGui( final World world, final EntityPlayer player, final ItemStack wirelessTerminal )
	{
		// Ignored client side
		if( world.isRemote )
		{
			return;
		}

		// Ensure the stack is a valid terminal
		if( ( wirelessTerminal == null ) || !( wirelessTerminal.getItem() instanceof ItemWirelessEssentiaTerminal ) )
		{
			// Invalid terminal
			return;
		}

		// Get the terminal item.
		ItemWirelessEssentiaTerminal itemWTerm = (ItemWirelessEssentiaTerminal)wirelessTerminal.getItem();

		// Ensure the terminal has power
		if( itemWTerm.getAECurrentPower( wirelessTerminal ) == 0 )
		{
			// Terminal is dead
			player.addChatMessage( PlayerMessages.DeviceNotPowered.get() );
			return;
		}

		// Ensure the terminal is linked
		if( !itemWTerm.isTerminalLinked( wirelessTerminal ) )
		{
			// Unlinked terminal
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the encryption key
		long encryptionKey;
		try
		{
			encryptionKey = Long.parseLong( itemWTerm.getEncryptionKey( wirelessTerminal ) );
		}
		catch( NumberFormatException e )
		{
			// Invalid security key
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the linked source
		Object source = AEApi.instance().registries().locateable().findLocateableBySerial( encryptionKey );

		// Ensure it is a security terminal
		if( !( source instanceof TileSecurity ) )
		{
			// Invalid security terminal
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the terminal
		TileSecurity securityHost = (TileSecurity)source;

		// Get the grid
		IGrid hostGrid;
		try
		{
			hostGrid = securityHost.getGridNode( ForgeDirection.UNKNOWN ).getGrid();
		}
		catch( Exception e )
		{
			// Can not find the grid
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
			return;
		}

		// Get the AP's
		IMachineSet accessPoints = hostGrid.getMachines( TileWireless.class );

		// Loop over AP's and see if any are close enough to communicate with
		for( IGridNode APNode : accessPoints )
		{
			// Get the AP
			IWirelessAccessPoint AP = (IWirelessAccessPoint)APNode.getMachine();

			// Is the AP active?
			if( AP.isActive() )
			{
				// Is the player close enough to the AP?
				if( this.isAPInRangeOfPlayer( AP.getLocation(), AP.getRange(), player ) )
				{
					// Launch the gui
					TEGuiHandler.launchGui( TEGuiHandler.WIRELESS_TERMINAL_ID, player, world, (int)player.posX, (int)player.posY, (int)player.posZ,
						new Object[] { new HandlerWirelessEssentiaTerminal( AP ) } );

					// All done.
					return;
				}
			}
		}

		// No AP's were close enough
		if( accessPoints.isEmpty() )
		{
			player.addChatMessage( PlayerMessages.CommunicationError.get() );
		}
		else
		{
			player.addChatMessage( PlayerMessages.OutOfRange.get() );
		}

	}

	/**
	 * Adds information about the wireless terminal to its tooltip.
	 */
	@Override
	public void addInformation( final ItemStack wirelessTerminal, final EntityPlayer player, final List tooltip, final boolean advancedItemTooltips )
	{
		// Get the current power
		double storedPower = this.getAECurrentPower( wirelessTerminal );

		// Add the energy amount
		tooltip.add( String.format( "%s %.0f AE - %.1f%%", GuiText.StoredEnergy.getLocal(), storedPower,
			100.0D * ( storedPower / ItemWirelessEssentiaTerminal.POWER_STORAGE ) ) );

		// Add link info
		if( this.isTerminalLinked( wirelessTerminal ) )
		{
			// Is linked
			tooltip.add( GuiText.Linked.getLocal() );
		}
		else
		{
			// Is not linked
			tooltip.add( GuiText.Unlinked.getLocal() );

		}
	}

	/**
	 * Takes power from the wireless terminal.
	 * 
	 * @param wirelessTerminal
	 * @param amount
	 * @return The amount of power extracted.
	 */
	@Override
	public double extractAEPower( final ItemStack wirelessTerminal, final double amount )
	{
		// Get the amount of stored power
		double storedPower = this.getAECurrentPower( wirelessTerminal );

		// Is there any power stored?
		if( storedPower == 0 )
		{
			// Terminal is dead
			return 0;
		}

		// Calculate the amount of power that can be extracted
		double canExtractAmount = Math.min( amount, storedPower );

		// Adjust the stored power
		storedPower = storedPower - canExtractAmount;

		// Was the power completely drained?
		if( storedPower <= 0 )
		{
			// Remove the power tag
			this.getOrCreateCompoundTag( wirelessTerminal ).removeTag( ItemWirelessEssentiaTerminal.NBT_STORED_POWER );

		}
		else
		{
			// Set the stored power
			this.getOrCreateCompoundTag( wirelessTerminal ).setDouble( ItemWirelessEssentiaTerminal.NBT_STORED_POWER, storedPower );
		}

		// Return the amount of power extracted.
		return canExtractAmount;
	}

	/**
	 * Gets the amount of power stored in the wireless terminal.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public double getAECurrentPower( final ItemStack wirelessTerminal )
	{
		// Has power usage been disabled?
		if( ItemWirelessEssentiaTerminal.GLOBAL_POWER_MULTIPLIER == 0 )
		{
			// Lie and say we are full power.
			return ItemWirelessEssentiaTerminal.POWER_STORAGE;
		}

		// Get the data tag
		NBTTagCompound data = this.getOrCreateCompoundTag( wirelessTerminal );

		// Is there any stored power?
		if( !data.hasKey( ItemWirelessEssentiaTerminal.NBT_STORED_POWER ) )
		{
			// Terminal has no stored power
			return 0;
		}

		// Return the current amount of power
		return data.getDouble( ItemWirelessEssentiaTerminal.NBT_STORED_POWER );
	}

	/**
	 * Gets the maximum amount of power the terminal can store.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public double getAEMaxPower( final ItemStack wirelessTerminal )
	{
		return ItemWirelessEssentiaTerminal.POWER_STORAGE;
	}

	/**
	 * Gets the percentage the terminal is charged.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public double getDurabilityForDisplay( final ItemStack wirelessTerminal )
	{
		return 1.0D - this.getAECurrentPower( wirelessTerminal ) / ItemWirelessEssentiaTerminal.POWER_STORAGE;
	}

	/**
	 * Gets the encryption, or source, key for the specified terminal.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public String getEncryptionKey( final ItemStack wirelessTerminal )
	{
		// Ensure the terminal has a tag
		if( wirelessTerminal.hasTagCompound() )
		{
			// Get the security terminal source key
			String sourceKey = wirelessTerminal.getTagCompound().getString( ItemWirelessEssentiaTerminal.NBT_AE_SOURCE_KEY );

			// Ensure the source is not empty nor null
			if( ( sourceKey != null ) && ( !sourceKey.isEmpty() ) )
			{
				// The terminal is linked.
				return sourceKey;
			}
		}

		// Terminal is unlinked.
		return "";
	}

	/**
	 * Can charge and drain.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public AccessRestriction getPowerFlow( final ItemStack wirelessTerminal )
	{
		return AccessRestriction.READ_WRITE;
	}

	/**
	 * Gets the terminals sorting mode.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	public ComparatorMode getSortingMode( final ItemStack wirelessTerminal )
	{
		// Does the terminal have a data tag?
		if( wirelessTerminal.hasTagCompound() )
		{
			// Get the data tag
			NBTTagCompound dataTagCompound = wirelessTerminal.getTagCompound();

			// Does the tag have the sorting mode stored?
			if( dataTagCompound.hasKey( ItemWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE ) )
			{
				return ComparatorMode.valueOf( dataTagCompound.getString( ItemWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE ) );
			}
		}

		return ComparatorMode.MODE_ALPHABETIC;
	}

	/**
	 * Gets the distance the specified player is from the AP.
	 * 
	 * @param APLocation
	 * @param player
	 * @return
	 */
	public double getSquaredPlayerDistanceFromAP( final DimensionalCoord APLocation, final EntityPlayer player )
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
	 * Adds an uncharged, and fully charged wireless terminal to the creative
	 * tab.
	 */
	@Override
	public void getSubItems( final Item item, final CreativeTabs tab, final List subItems )
	{
		// Create the uncharged and charged items
		ItemStack unchargedTerm = new ItemStack( item, 1, 0 );
		ItemStack chargedTerm = unchargedTerm.copy();

		// Add charge
		this.injectAEPower( chargedTerm, ItemWirelessEssentiaTerminal.POWER_STORAGE );

		// Add them
		subItems.add( unchargedTerm );
		subItems.add( chargedTerm );
	}

	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item." + ItemEnum.WIRELESS_TERMINAL.getInternalName();
	}

	/**
	 * Adds power to the wireless terminal.
	 * 
	 * @param wirelessTerminal
	 * @param amount
	 * @return Amount of power that was not injected.
	 */
	@Override
	public double injectAEPower( final ItemStack wirelessTerminal, final double amount )
	{
		// Get the current amount of power
		double storedPower = this.getAECurrentPower( wirelessTerminal );

		// Calculate the amount of power that can be injected
		double canInjectAmount = Math.min( amount, ItemWirelessEssentiaTerminal.POWER_STORAGE - storedPower );

		// Can any amount be injected?
		if( canInjectAmount > 0 )
		{
			// Add to the stored power
			this.getOrCreateCompoundTag( wirelessTerminal ).setDouble( ItemWirelessEssentiaTerminal.NBT_STORED_POWER, storedPower + canInjectAmount );
		}

		// Return the amount injected
		return amount - canInjectAmount;
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
	public boolean isAPInRangeOfPlayer( final DimensionalCoord APLocation, final double APRange, final EntityPlayer player )
	{
		// Is the AP and the player in the same world?
		if( !APLocation.isInWorld( player.worldObj ) )
		{
			return false;
		}

		// Calculate the square distance
		double squareDistance = this.getSquaredPlayerDistanceFromAP( APLocation, player );

		// Return if close enough to use AP
		return squareDistance <= ( APRange * APRange );
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public boolean isDamaged( final ItemStack stack )
	{
		return true;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	/**
	 * Returns true if the wireless terminal is linked to a network.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	public boolean isTerminalLinked( final ItemStack wirelessTerminal )
	{
		return( !this.getEncryptionKey( wirelessTerminal ).isEmpty() );
	}

	/**
	 * Opens the wireless terminal.
	 * 
	 * @param itemStack
	 * @param world
	 * @param entityPlayer
	 * @return
	 */
	@Override
	public ItemStack onItemRightClick( final ItemStack itemStack, final World world, final EntityPlayer player )
	{
		// Open the gui
		this.openWirelessTerminalGui( world, player, itemStack );

		return itemStack;

	}

	/**
	 * Registers and sets the wireless terminal icon.
	 */
	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		this.itemIcon = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":wireless.essentia.terminal" );
	}

	@Override
	public void setDamage( final ItemStack stack, final int damage )
	{
	}

	/**
	 * Sets the encryption, or source, key for the specified terminal.
	 * 
	 * @param wirelessTerminal
	 * @param sourceKey
	 * @param name
	 * Ignored.
	 */
	@Override
	public void setEncryptionKey( final ItemStack wirelessTerminal, final String sourceKey, final String name )
	{
		// Set the key
		this.getOrCreateCompoundTag( wirelessTerminal ).setString( ItemWirelessEssentiaTerminal.NBT_AE_SOURCE_KEY, sourceKey );
	}

	/**
	 * Sets the terminals sorting mode.
	 * 
	 * @param wirelessTerminal
	 * @param mode
	 */
	public void setSortingMode( final ItemStack wirelessTerminal, final ComparatorMode mode )
	{
		// Get the data tag
		NBTTagCompound dataTag = this.getOrCreateCompoundTag( wirelessTerminal );

		// Set the sorting mode
		dataTag.setString( ItemWirelessEssentiaTerminal.NBT_KEY_SORTING_MODE, mode.name() );
	}

	/**
	 * Always show the durability bar.
	 * 
	 * @param wirelessTerminal
	 * @return
	 */
	@Override
	public boolean showDurabilityBar( final ItemStack wirelessTerminal )
	{
		return true;
	}
}
