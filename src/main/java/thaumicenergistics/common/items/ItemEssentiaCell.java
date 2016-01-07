package thaumicenergistics.common.items;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import thaumicenergistics.api.storage.IAspectStack;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.common.inventory.HandlerItemEssentiaCellCreative;
import thaumicenergistics.common.registries.ItemEnum;
import thaumicenergistics.common.registries.ThEStrings;
import thaumicenergistics.common.storage.AspectStackComparator;
import thaumicenergistics.common.storage.EnumEssentiaStorageTypes;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.localization.GuiText;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEssentiaCell
	extends Item
	implements ICellHandler
{
	/**
	 * Status of the cell.
	 */
	private static final int CELL_STATUS_MISSING = 0, CELL_STATUS_HAS_ROOM = 1, CELL_STATUS_TYPES_FULL = 2, CELL_STATUS_FULL = 3;

	/**
	 * Icons for each type.
	 */
	private IIcon[] icons;

	public ItemEssentiaCell()
	{
		// Add the handler to AE2
		AEApi.instance().registries().cell().addCellHandler( this );

		// Set max stack size to 1
		this.setMaxStackSize( 1 );

		// No damage
		this.setMaxDamage( 0 );

		// Has sub-types
		this.setHasSubtypes( true );
	}

	/**
	 * Adds the contents of the cell to the description tooltip.
	 * 
	 * @param cellHandler
	 * @param displayList
	 * @param player
	 */
	private void addContentsToCellDescription( final HandlerItemEssentiaCell cellHandler, final List displayList, final EntityPlayer player )
	{
		// Get the list of stored aspects
		List<IAspectStack> cellAspects = cellHandler.getStoredEssentia();

		// Sort the list
		Collections.sort( cellAspects, new AspectStackComparator() );

		for( IAspectStack currentStack : cellAspects )
		{
			if( currentStack != null )
			{
				// Get the chat color
				String aspectChatColor = currentStack.getChatColor();

				// Does this aspect have color?
				if( aspectChatColor == null )
				{
					// It does not, set to white
					aspectChatColor = EnumChatFormatting.WHITE.toString();
				}

				// Build the display string
				String aspectInfo = String.format( "%s%s%s x %d", aspectChatColor, currentStack.getAspectName( player ),
					EnumChatFormatting.WHITE.toString(), currentStack.getStackSize() );

				// Add to the list
				displayList.add( aspectInfo );
			}
		}

	}

	/**
	 * Creates the cell tooltip.
	 */
	@Override
	public void addInformation( final ItemStack essentiaCell, final EntityPlayer player, final List displayList, final boolean advancedItemTooltips )
	{
		// Get the contents of the cell
		IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell()
						.getCellInventory( essentiaCell, null, StorageChannel.FLUIDS );

		// Ensure we have a cell inventory handler
		if( !( handler instanceof HandlerItemEssentiaCell ) )
		{
			return;
		}

		// Cast to cell inventory handler
		HandlerItemEssentiaCell cellHandler = (HandlerItemEssentiaCell)handler;

		// Create the bytes tooltip
		String bytesTip = String.format( ThEStrings.Tooltip_CellBytes.getLocalized(), cellHandler.getUsedBytes(), cellHandler.getTotalBytes() );

		// Create the types tooltip
		String typesTip = String.format( ThEStrings.Tooltip_CellTypes.getLocalized(), cellHandler.getUsedTypes(), cellHandler.getTotalTypes() );

		// Add the tooltips
		displayList.add( bytesTip );
		displayList.add( typesTip );

		// Is the cell pre-formated?
		if( cellHandler.isPartitioned() )
		{
			displayList.add( GuiText.Partitioned.getLocal() );
		}

		// Does the cell have anything stored?
		if( cellHandler.getUsedTypes() > 0 )
		{
			// Is shift being held?
			if( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || ( Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) )
			{
				// Add information about the essentia types in the cell
				this.addContentsToCellDescription( cellHandler, displayList, player );
			}
			else
			{
				// Let the user know they can hold shift
				displayList.add( EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString() +
								ThEStrings.Tooltip_ItemStackDetails.getLocalized() );
			}
		}

	}

	/**
	 * How much power is required by the cell each tick.
	 */
	@Override
	public double cellIdleDrain( final ItemStack itemStack, final IMEInventory handler )
	{
		return EnumEssentiaStorageTypes.fromIndex[itemStack.getItemDamage()].idleAEPowerDrain;
	}

	/**
	 * Gets a handler for the cell.
	 */
	@Override
	public IMEInventoryHandler getCellInventory( final ItemStack essentiaCell, final ISaveProvider saveProvider, final StorageChannel channel )
	{
		// Ensure the channel is fluid and there is an appropriate item.
		if( ( channel != StorageChannel.FLUIDS ) || !( essentiaCell.getItem() instanceof ItemEssentiaCell ) )
		{
			return null;
		}

		// Is the type creative?
		if( essentiaCell.getItemDamage() == EnumEssentiaStorageTypes.Type_Creative.index )
		{
			// Return a creative handler.
			return new HandlerItemEssentiaCellCreative( essentiaCell, saveProvider );
		}

		// Return a standard handler.
		return new HandlerItemEssentiaCell( essentiaCell, saveProvider );
	}

	/**
	 * Gets the cell's icon.
	 */
	@Override
	public IIcon getIconFromDamage( final int dmg )
	{
		// Clamp the index
		int index = MathHelper.clamp_int( dmg, 0, EnumEssentiaStorageTypes.fromIndex.length - 1 );

		// Return the icon
		return this.icons[index];
	}

	/**
	 * Gets the rarity of the cell.
	 */
	@Override
	public EnumRarity getRarity( final ItemStack itemStack )
	{
		// Get the index based off of the meta data
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, EnumEssentiaStorageTypes.fromIndex.length - 1 );

		// Return the rarity
		return EnumEssentiaStorageTypes.fromIndex[index].rarity;
	}

	/**
	 * Gets the status of the cell.
	 * Full | Type Full | Has Room
	 */
	@Override
	public int getStatusForCell( final ItemStack essentiaCell, final IMEInventory handler )
	{
		// Do we have a handler?
		if( handler == null )
		{
			return ItemEssentiaCell.CELL_STATUS_MISSING;
		}

		// Get the inventory handler
		HandlerItemEssentiaCell cellHandler = (HandlerItemEssentiaCell)handler;

		// Creative?
		if( cellHandler.isCreative() )
		{
			return ItemEssentiaCell.CELL_STATUS_TYPES_FULL;
		}

		// Full bytes?
		if( cellHandler.getUsedBytes() == cellHandler.getTotalBytes() )
		{
			return ItemEssentiaCell.CELL_STATUS_FULL;
		}

		// Full types?
		if( cellHandler.getUsedTypes() == cellHandler.getTotalTypes() )
		{
			return ItemEssentiaCell.CELL_STATUS_TYPES_FULL;
		}

		return ItemEssentiaCell.CELL_STATUS_HAS_ROOM;

	}

	/**
	 * Gets the different cell sizes and places them on the creative tab.
	 */
	@Override
	public void getSubItems( final Item item, final CreativeTabs creativeTab, final List listSubItems )
	{
		for( EnumEssentiaStorageTypes type : EnumEssentiaStorageTypes.fromIndex )
		{
			listSubItems.add( type.getCell() );
		}
	}

	/**
	 * ME Chest icon
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTopTexture_Dark()
	{
		return BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[0];
	}

	/**
	 * ME Chest icon
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTopTexture_Light()
	{
		return BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[2];
	}

	/**
	 * ME Chest icon
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTopTexture_Medium()
	{
		return BlockTextureManager.ESSENTIA_TERMINAL.getTextures()[1];
	}

	@Override
	public String getUnlocalizedName()
	{
		return ThaumicEnergistics.MOD_ID + ".item.essentia.cell";
	}

	/**
	 * Name of the cell.
	 */
	@Override
	public String getUnlocalizedName( final ItemStack itemStack )
	{
		return EnumEssentiaStorageTypes.fromIndex[itemStack.getItemDamage()].cellName.getUnlocalized();
	}

	/**
	 * True if the specified item is an Essentia cell.
	 */
	@Override
	public boolean isCell( final ItemStack itemStack )
	{
		return itemStack.getItem() == this;
	}

	/**
	 * Maximum storage, in bytes, the cell can hold.
	 * 
	 * @param essentiaCell
	 * @return
	 */
	public int maxStorage( final ItemStack essentiaCell )
	{
		return EnumEssentiaStorageTypes.fromIndex[essentiaCell.getItemDamage()].capacity;
	}

	/**
	 * The maximum number of types each cell can hold.
	 * 
	 * @param essentiaCell
	 * @return
	 */
	public int maxTypes( final ItemStack essentiaCell )
	{
		return EnumEssentiaStorageTypes.fromIndex[essentiaCell.getItemDamage()].maxStoredTypes;
	}

	/**
	 * Attempts to remove the storage component.
	 */
	@Override
	public ItemStack onItemRightClick( final ItemStack essentiaCell, final World world, final EntityPlayer player )
	{
		// Ensure the player is sneaking(holding shift)
		if( !player.isSneaking() )
		{
			return essentiaCell;
		}

		// Ensure this is not a creative cell
		if( essentiaCell.getItemDamage() == EnumEssentiaStorageTypes.Type_Creative.index )
		{
			return essentiaCell;
		}

		// Get the handler
		IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell()
						.getCellInventory( essentiaCell, null, StorageChannel.FLUIDS );

		// Is it the correct handler type?
		if( !( handler instanceof HandlerItemEssentiaCell ) )
		{
			return essentiaCell;
		}

		// Cast
		HandlerItemEssentiaCell cellHandler = (HandlerItemEssentiaCell)handler;

		// If the cell is empty, and the player can hold the casing
		if( ( cellHandler.getUsedBytes() == 0 ) && ( player.inventory.addItemStackToInventory( ItemEnum.STORAGE_CASING.getStack() ) ) )
		{
			// Return the storage component
			return EnumEssentiaStorageTypes.fromIndex[essentiaCell.getItemDamage()].getComponent( 1 );
		}

		// Can not remove storage component, return the current cell as is.
		return essentiaCell;
	}

	/**
	 * Shows the cell GUI.
	 */
	@Override
	public void openChestGui( final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler, final IMEInventoryHandler inv,
								final ItemStack itemStack, final StorageChannel channel )
	{
		// Ensure this is the fluid channel
		if( channel != StorageChannel.FLUIDS )
		{
			return;
		}

		// Ensure we have a chest
		if( chest != null )
		{
			// Get a reference to the chest's inventories
			IStorageMonitorable monitorable = ( (IMEChest)chest ).getMonitorable( ForgeDirection.UNKNOWN, new PlayerSource( player, chest ) );

			// Ensure we got the inventories
			if( monitorable != null )
			{
				// Get the chest tile entity
				TileEntity chestEntity = (TileEntity)chest;

				// Show the terminal gui
				ThEGuiHandler.launchGui( ThEGuiHandler.ESSENTIA_CELL_ID, player, chestEntity.getWorldObj(), chestEntity.xCoord, chestEntity.yCoord,
					chestEntity.zCoord );
			}
		}

	}

	@Override
	public void registerIcons( final IIconRegister iconRegister )
	{
		// Create the icon array
		this.icons = new IIcon[EnumEssentiaStorageTypes.fromIndex.length];

		// Add each type
		for( int i = 0; i < this.icons.length; i++ )
		{
			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":essentia.cell." + EnumEssentiaStorageTypes.fromIndex[i].suffix );
		}
	}

}
