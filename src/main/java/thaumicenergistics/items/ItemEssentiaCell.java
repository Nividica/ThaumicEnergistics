package thaumicenergistics.items;

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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Keyboard;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.aspect.AspectStack;
import thaumicenergistics.aspect.AspectStackComparator;
import thaumicenergistics.gui.GuiHandler;
import thaumicenergistics.inventory.HandlerItemEssentiaCell;
import thaumicenergistics.registries.ItemEnum;
import thaumicenergistics.texture.BlockTextureManager;
import thaumicenergistics.util.GuiHelper;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;

// TODO: Partition support, ICellWorkbenchItem does not work.
public class ItemEssentiaCell
	extends ItemStorageBase
	implements ICellHandler
{
	private static final EnumRarity[] RARITIES = { EnumRarity.uncommon, EnumRarity.uncommon, EnumRarity.rare, EnumRarity.epic };

	private static final double[] IDLE_DRAIN_AMOUNTS = { 0.5D, 1.0D, 1.5D, 2.0D };

	private static final int MAX_TYPES = 8;

	public static final long CONVERSION_SIZE = 250L;

	private static final int CELL_STATUS_MISSING = 0;

	private static final int CELL_STATUS_HAS_ROOM = 1;

	private static final int CELL_STATUS_TYPES_FULL = 2;

	private static final int CELL_STATUS_FULL = 3;

	private IIcon[] icons;

	public ItemEssentiaCell()
	{
		AEApi.instance().registries().cell().addCellHandler( this );

		this.setMaxStackSize( 1 );

		this.setMaxDamage( 0 );

		this.setHasSubtypes( true );
	}

	private void addContentsToCellDescription( HandlerItemEssentiaCell cellHandler, List displayList, EntityPlayer player )
	{
		// Get the list of stored aspects
		List<AspectStack> cellAspects = cellHandler.getAvailableAspects();

		// Sort the list
		Collections.sort( cellAspects, new AspectStackComparator() );

		for( AspectStack currentStack : cellAspects )
		{
			if( currentStack != null )
			{
				// Get the chat color
				String aspectChatColor = currentStack.getChatColor();

				// Does this aspect have color?
				if( aspectChatColor != null )
				{
					// Add the color header
					aspectChatColor = GuiHelper.CHAT_COLOR_HEADER + aspectChatColor;
				}
				else
				{
					// It does not, set to gray
					aspectChatColor = EnumChatFormatting.GRAY.toString();
				}

				// Build the display string
				String aspectInfo = String.format( "%s%s%s x %d", aspectChatColor, currentStack.getAspectName( player ),
					EnumChatFormatting.GRAY.toString(), currentStack.amount );

				// Add to the list
				displayList.add( aspectInfo );
			}
		}

	}

	@Override
	public void addInformation( ItemStack essentiaCell, EntityPlayer player, List displayList, boolean advancedItemTooltips )
	{
		// TODO: Save provider??
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
		String bytesTip = String.format( StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.essentia.cell.bytes" ), new Object[] {
						cellHandler.usedBytes() / ItemEssentiaCell.CONVERSION_SIZE, cellHandler.totalBytes() / ItemEssentiaCell.CONVERSION_SIZE } );

		// Create the types tooltip
		String typesTip = String.format( StatCollector.translateToLocal( ThaumicEnergistics.MOD_ID + ".tooltip.essentia.cell.types" ), new Object[] {
						cellHandler.usedTypes(), cellHandler.totalTypes() } );

		// Add the tooltips
		displayList.add( bytesTip );
		displayList.add( typesTip );

		// Is the cell pre-formated?
		if( cellHandler.isPreformatted() )
		{
			displayList.add( StatCollector.translateToLocal( "Appeng.GuiITooltip.Partitioned" ) + " - " +
							StatCollector.translateToLocal( "Appeng.GuiITooltip.Precise" ) );
		}

		// Is shift being held?
		if( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || ( Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) ) )
		{
			// Add information about the essentia types in the cell
			this.addContentsToCellDescription( cellHandler, displayList, player );
		}

	}

	@Override
	public double cellIdleDrain( ItemStack itemStack, IMEInventory handler )
	{
		return ItemEssentiaCell.IDLE_DRAIN_AMOUNTS[itemStack.getItemDamage()];
	}

	@Override
	public IMEInventoryHandler getCellInventory( ItemStack essentiaCell, ISaveProvider saveProvider, StorageChannel channel )
	{
		if( ( channel != StorageChannel.FLUIDS ) || ( essentiaCell.getItem() != this ) )
		{
			return null;
		}

		return new HandlerItemEssentiaCell( essentiaCell );
	}

	@Override
	public IIcon getIconFromDamage( int dmg )
	{
		int index = MathHelper.clamp_int( dmg, 0, ItemStorageBase.SUFFIXES.length );

		return this.icons[index];
	}

	@Override
	public EnumRarity getRarity( ItemStack itemStack )
	{
		// Get the index based off of the meta data
		int index = MathHelper.clamp_int( itemStack.getItemDamage(), 0, ItemEssentiaCell.RARITIES.length );

		// Return the rarity
		return ItemEssentiaCell.RARITIES[index];
	}

	@Override
	public int getStatusForCell( ItemStack essentiaCell, IMEInventory handler )
	{
		// Do we have a handler?
		if( handler == null )
		{
			return ItemEssentiaCell.CELL_STATUS_MISSING;
		}

		// Get the inventory handler
		HandlerItemEssentiaCell cellHandler = (HandlerItemEssentiaCell)handler;

		// Full bytes?
		if( cellHandler.usedBytes() == cellHandler.totalBytes() )
		{
			return ItemEssentiaCell.CELL_STATUS_FULL;
		}

		// Full types?
		if( cellHandler.usedTypes() == cellHandler.totalTypes() )
		{
			return ItemEssentiaCell.CELL_STATUS_TYPES_FULL;
		}

		return ItemEssentiaCell.CELL_STATUS_HAS_ROOM;

	}

	@Override
	public void getSubItems( Item item, CreativeTabs creativeTab, List listSubItems )
	{
		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			listSubItems.add( new ItemStack( item, 1, i ) );

		}
	}

	@Override
	public IIcon getTopTexture_Dark()
	{
		return BlockTextureManager.ESSENTIA_STORAGE_CELL_CHEST.getTexture();
	}

	@Override
	public IIcon getTopTexture_Light()
	{
		return BlockTextureManager.ESSENTIA_STORAGE_CELL_CHEST.getTexture();
	}

	@Override
	public IIcon getTopTexture_Medium()
	{
		return BlockTextureManager.ESSENTIA_STORAGE_CELL_CHEST.getTexture();
	}

	@Override
	public String getUnlocalizedName( ItemStack itemStack )
	{
		return ThaumicEnergistics.MOD_ID + ".item.essentia.cell." + ItemStorageBase.SUFFIXES[itemStack.getItemDamage()];
	}

	@Override
	public boolean isCell( ItemStack itemStack )
	{
		return itemStack.getItem() == this;
	}

	public int maxStorage( ItemStack essentiaCell )
	{
		return ItemStorageBase.SIZES[Math.max( 0, essentiaCell.getItemDamage() )];
	}

	public int maxTypes( ItemStack itemStack )
	{
		return ItemEssentiaCell.MAX_TYPES;
	}

	@Override
	public ItemStack onItemRightClick( ItemStack essentiaCell, World world, EntityPlayer player )
	{
		// Ensure the player is sneaking(holding shift)
		if( !player.isSneaking() )
		{
			return essentiaCell;
		}

		// TODO: SaveProvider??
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
		if( ( cellHandler.usedBytes() == 0 ) && ( player.inventory.addItemStackToInventory( ItemEnum.STORAGE_CASING.getItemStackWithSize( 1 ) ) ) )
		{
			// Return the storage component
			return ItemEnum.STORAGE_COMPONENT.getItemStackWithDamage( essentiaCell.getItemDamage() );
		}

		// Can not remove storage component, return the current cell as is.
		return essentiaCell;
	}

	@Override
	public void openChestGui( EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack itemStack,
								StorageChannel channel )
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
				GuiHandler.launchGui( GuiHandler.ESSENTIA_CELL_ID, player, chestEntity.getWorldObj(), chestEntity.xCoord, chestEntity.yCoord,
					chestEntity.zCoord );
			}
		}

	}

	@Override
	public void registerIcons( IIconRegister iconRegister )
	{
		this.icons = new IIcon[ItemStorageBase.SUFFIXES.length];

		for( int i = 0; i < ItemStorageBase.SUFFIXES.length; i++ )
		{
			this.icons[i] = iconRegister.registerIcon( ThaumicEnergistics.MOD_ID + ":essentia.cell." + ItemStorageBase.SUFFIXES[i] );
		}
	}

}
