package thaumicenergistics.items;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.api.IWirelessEssentiaTerminal;
import thaumicenergistics.api.TEApi;
import thaumicenergistics.inventory.HandlerWirelessEssentiaTerminal;
import thaumicenergistics.registries.ItemEnum;
import appeng.api.config.AccessRestriction;
import appeng.api.config.PowerMultiplier;
import appeng.core.localization.GuiText;

public class ItemWirelessEssentiaTerminal
	extends Item
	implements IWirelessEssentiaTerminal
{
	/**
	 * NBT keys
	 */
	private static final String NBT_AE_SOURCE_KEY = "SourceKey", NBT_STORED_POWER = "StoredPower";

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
		if( HandlerWirelessEssentiaTerminal.isTerminalLinked( this, wirelessTerminal ) )
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
	 * Gets the data tag used to save the terminal settings a power level.
	 */
	@Override
	public NBTTagCompound getWETerminalTag( final ItemStack wirelessTerminal )
	{
		return this.getOrCreateCompoundTag( wirelessTerminal );
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
		TEApi.instance().openWirelessTerminalGui( player, this );

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
