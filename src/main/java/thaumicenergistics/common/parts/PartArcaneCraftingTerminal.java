package thaumicenergistics.common.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.api.grid.ICraftingIssuerHost;
import thaumicenergistics.api.grid.IDigiVisSource;
import thaumicenergistics.client.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.client.textures.BlockTextureManager;
import thaumicenergistics.common.ThEGuiHandler;
import thaumicenergistics.common.ThaumicEnergistics;
import thaumicenergistics.common.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.common.network.packet.server.Packet_S_ChangeGui;
import thaumicenergistics.common.registries.AEPartsEnum;
import thaumicenergistics.common.registries.EnumCache;
import thaumicenergistics.common.utils.EffectiveSide;
import thaumicenergistics.common.utils.ThEUtils;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEColor;
import appeng.api.util.IConfigManager;
import appeng.items.storage.ItemViewCell;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartArcaneCraftingTerminal
	extends ThERotateablePart
	implements IInventory, IGridTickable, ICraftingIssuerHost, ITerminalHost
{
	/**
	 * Number of slots in the internal inventory
	 */
	private static final int MY_INVENTORY_SIZE = 20;

	/**
	 * Index of the wand, result, and view slot(s)
	 */
	public static final int WAND_SLOT_INDEX = 9, RESULT_SLOT_INDEX = 10, VIEW_SLOT_MIN = 11, VIEW_SLOT_MAX = 15, ARMOR_SLOT_MIN = 16;

	/**
	 * Default sorting order.
	 */
	public static final SortOrder DEFAULT_SORT_ORDER = SortOrder.NAME;

	/**
	 * Default sorting direction.
	 */
	public static final SortDir DEFAULT_SORT_DIR = SortDir.ASCENDING;

	/**
	 * Default view mode.
	 */
	public static final ViewItems DEFAULT_VIEW_MODE = ViewItems.ALL;

	/**
	 * Inventory name
	 */
	private static final String INVENTORY_NBT_KEY = "TEACT_Inventory";

	/**
	 * Key used for reading/writing inventory slots to NBT
	 */
	private static final String SLOT_NBT_KEY = "Slot#";

	/**
	 * Key used for reading/writing the sorting order.
	 */
	private static final String SORT_ORDER_NBT_KEY = "SortOrder";

	/**
	 * Key used for reading/writing the sorting direction.
	 */
	private static final String SORT_DIRECTION_NBT_KEY = "SortDirection";

	/**
	 * Key used for reading/writing the view mode.
	 */
	private static final String VIEW_MODE_NBT_KEY = "ViewMode";

	/**
	 * Key used for reading/writing the vis info
	 */
	private static final String VIS_INTERFACE_NBT_KEY = "VisInterface";

	/**
	 * How much AE power is required to keep the part active.
	 */
	private static final double IDLE_POWER_DRAIN = 0.5D;

	/**
	 * Inventory slots
	 */
	private final ItemStack[] slots = new ItemStack[PartArcaneCraftingTerminal.MY_INVENTORY_SIZE];

	/**
	 * Containers wishing to be notified on inventory changes
	 */
	private List<ContainerPartArcaneCraftingTerminal> listeners = new ArrayList<ContainerPartArcaneCraftingTerminal>();

	/**
	 * How the items are sorted.
	 */
	private SortOrder sortingOrder = PartArcaneCraftingTerminal.DEFAULT_SORT_ORDER;

	/**
	 * What direction are the items sorted.
	 */
	private SortDir sortingDirection = PartArcaneCraftingTerminal.DEFAULT_SORT_DIR;

	/**
	 * What items types are visible.
	 */
	private ViewItems viewMode = PartArcaneCraftingTerminal.DEFAULT_VIEW_MODE;

	/**
	 * Data pertaining to the linked digi-vis source
	 */
	private DigiVisSourceData visSourceInfo = new DigiVisSourceData();

	/**
	 * Creates the terminal
	 */
	public PartArcaneCraftingTerminal()
	{
		// Call super
		super( AEPartsEnum.ArcaneCraftingTerminal, SecurityPermissions.EXTRACT, SecurityPermissions.INJECT );
	}

	/**
	 * Validates that a slot is inbounds of the inventory.
	 * 
	 * @param slotIndex
	 * @return
	 */
	private boolean isSlotInRange( final int slotIndex )
	{
		// Is the slot in range?
		return( ( slotIndex >= 0 ) && ( slotIndex < PartArcaneCraftingTerminal.MY_INVENTORY_SIZE ) );
	}

	/**
	 * Notifies all listeners that our inventory contents
	 * have changed.
	 * 
	 * @param slotIndex
	 */
	private void notifyListeners( final int slotIndex )
	{
		// Did the crafting slots, wand, or results slot change?
		boolean notifyCrafting = ( slotIndex <= PartArcaneCraftingTerminal.RESULT_SLOT_INDEX );

		// Loop over all listeners
		for( ContainerPartArcaneCraftingTerminal listener : this.listeners )
		{
			// Ensure the listener is still there
			if( listener != null )
			{
				// Crafting changes?
				if( notifyCrafting )
				{
					listener.onCraftMatrixChanged( this );
				}
				else
				{
					listener.onViewCellChange();
				}
			}
		}
	}

	/**
	 * Distance cable should extend to meet this block
	 */
	@Override
	public int cableConnectionRenderTo()
	{
		return 3;
	}

	@Override
	public void closeInventory()
	{
		// Ignored
	}

	/**
	 * Decreases the size of the itemstack in the specified
	 * slot by the specified amount, and returns the itemstack.
	 * Can be null.
	 */
	@Override
	public ItemStack decrStackSize( final int slotIndex, final int amount )
	{
		ItemStack returnStack = null;

		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Get the stack
			ItemStack slotStack = this.slots[slotIndex];

			// Is the slot not null?
			if( slotStack != null )
			{

				// Is the amount for more than or all of the slot?
				if( amount >= slotStack.stackSize )
				{
					// Set the return to a copy of the stack
					returnStack = slotStack.copy();

					// Set stack size to 0
					this.slots[slotIndex].stackSize = 0;
				}
				else
				{
					// Split the slot stack
					returnStack = slotStack.splitStack( amount );
				}

				// Is the size now 0?
				if( this.slots[slotIndex].stackSize == 0 )
				{
					// Null it out
					this.slots[slotIndex] = null;
				}

				// Notify the containers
				this.notifyListeners( slotIndex );
			}
		}

		return returnStack;
	}

	/**
	 * Collision boxes
	 */
	@Override
	public void getBoxes( final IPartCollisionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	@Override
	public IIcon getBreakingTexture()
	{
		return BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0];
	}

	/**
	 * Gets the GUI associated with this part
	 */
	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return new GuiArcaneCraftingTerminal( this, player );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return null;
	}

	/**
	 * Determines if items should be dropped on the ground when
	 * the part has been removed.
	 */
	@Override
	public void getDrops( final List<ItemStack> drops, final boolean wrenched )
	{
		// Were we wrenched?
		if( wrenched )
		{
			// Inventory is saved when wrenched
			return;
		}

		// Loop over inventory
		for( int slotIndex = 0; slotIndex < PartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotIndex++ )
		{
			// Skip if this is the output slot
			if( slotIndex == PartArcaneCraftingTerminal.RESULT_SLOT_INDEX )
			{
				continue;
			}

			// Get the stack at this index
			ItemStack slotStack = this.slots[slotIndex];

			// Did we get anything?
			if( slotStack != null )
			{
				// Add to drops
				drops.add( slotStack );
			}
		}
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory()
	{
		// Ignored
		return null;
	}

	@Override
	public ItemStack getIcon()
	{
		return this.associatedItem;
	}

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return PartArcaneCraftingTerminal.IDLE_POWER_DRAIN;
	}

	/**
	 * Gets the inventory name.
	 */
	@Override
	public String getInventoryName()
	{
		return ThaumicEnergistics.MOD_ID + ".arcane.crafting.terminal.inventory";
	}

	/**
	 * Maximum number of allowed items per stack.
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory()
	{
		return this.getGridBlock().getItemMonitor();
	}

	/**
	 * Light level based on if the ACT is on or off.
	 */
	@Override
	public int getLightLevel()
	{
		return( this.isActive() ? ThEPartBase.ACTIVE_TERMINAL_LIGHT_LEVEL : 0 );
	}

	/**
	 * Gets the container associated with this part.
	 */
	@Override
	public Object getServerGuiElement( final EntityPlayer player )
	{
		return new ContainerPartArcaneCraftingTerminal( this, player );
	}

	/**
	 * Returns the internal inventory size.
	 */
	@Override
	public int getSizeInventory()
	{
		return PartArcaneCraftingTerminal.MY_INVENTORY_SIZE;
	}

	/**
	 * Returns the stored sorting direction.
	 * 
	 * @return
	 */
	public SortDir getSortingDirection()
	{
		return this.sortingDirection;
	}

	/**
	 * Returns the stored sorting order.
	 * 
	 * @return
	 */
	public SortOrder getSortingOrder()
	{
		return this.sortingOrder;
	}

	/**
	 * Gets the itemstack in the specified slot index.
	 * Can be null.
	 */
	@Override
	public ItemStack getStackInSlot( final int slotIndex )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Return the contents of the slot
			return this.slots[slotIndex];
		}

		// Return null
		return null;
	}

	/**
	 * Returns the itemstack in the specified slot, can be null.
	 */
	@Override
	public ItemStack getStackInSlotOnClosing( final int slotIndex )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			return this.slots[slotIndex];
		}

		return null;
	}

	@Override
	public TickingRequest getTickingRequest( final IGridNode grid )
	{
		// We would like a tick ever 2 to 20 MC ticks
		return new TickingRequest( 2, 20, false, false );
	}

	/**
	 * Returns the view mode.
	 * 
	 * @return
	 */
	public ViewItems getViewMode()
	{
		return this.viewMode;
	}

	/**
	 * Gets the world this inventory is in.
	 * 
	 * @return
	 */
	public World getWorldObj()
	{
		return this.getHostTile().getWorldObj();
	}

	/**
	 * Returns true.
	 */
	@Override
	public boolean hasCustomInventoryName()
	{
		return true;
	}

	/**
	 * Determines if the specified itemstack is valid for the slot index.
	 */
	@Override
	public boolean isItemValidForSlot( final int slotIndex, final ItemStack proposedStack )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Is the stack null?
			if( proposedStack == null )
			{
				// Can always remove from slot
				return true;
			}

			// Is this the wand slot?
			if( slotIndex == PartArcaneCraftingTerminal.WAND_SLOT_INDEX )
			{
				// Is the item a wand?
				return ThEUtils.isItemValidWand( proposedStack, false );
			}
			// Is this a view slot?
			if( ( slotIndex >= PartArcaneCraftingTerminal.VIEW_SLOT_MIN ) && ( slotIndex <= PartArcaneCraftingTerminal.VIEW_SLOT_MAX ) )
			{
				// Is the stack a view slot?
				return( proposedStack.getItem() instanceof ItemViewCell );

			}

			// Unrestricted slot
			return true;
		}

		// Out of range
		return false;
	}

	@Override
	public boolean isUseableByPlayer( final EntityPlayer player )
	{
		return this.isPartUseableByPlayer( player );
	}

	@Override
	public void launchGUI( final EntityPlayer player )
	{
		TileEntity host = this.getHostTile();

		// Is this server side?
		if( EffectiveSide.isServerSide() )
		{
			// Launch the gui
			ThEGuiHandler.launchGui( this, player, host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );
		}
		else
		{
			// Ask the server to change the GUI
			Packet_S_ChangeGui.sendGuiChangeToPart( this, player, host.getWorldObj(), host.xCoord, host.yCoord, host.zCoord );
		}
	}

	/**
	 * Ensures the inventory is saved.
	 */
	@Override
	public void markDirty()
	{
		this.markForSave();
	}

	/**
	 * Player right-clicked the terminal.
	 */
	@Override
	public boolean onActivate( final EntityPlayer player, final Vec3 position )
	{
		// Get what the player is holding
		ItemStack playerHolding = player.inventory.getCurrentItem();

		// Are they holding a memory card?
		if( ( playerHolding != null ) && ( playerHolding.getItem() instanceof IMemoryCard ) )
		{
			// Get the memory card
			IMemoryCard memoryCard = (IMemoryCard)playerHolding.getItem();

			// Get the stored name
			String settingsName = memoryCard.getSettingsName( playerHolding );

			// Does it contain the data about a vis source?
			if( settingsName.equals( DigiVisSourceData.SOURCE_UNLOC_NAME ) )
			{
				// Get the data
				NBTTagCompound data = memoryCard.getData( playerHolding );

				// Load the info
				this.visSourceInfo.readFromNBT( data );

				// Ensure there was valid data
				if( this.visSourceInfo.hasSourceData() )
				{
					// Inform the user
					memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
				}

				// Mark that we need to save
				this.markDirty();
			}
			// Is the memory card empty?
			else if( settingsName.equals( "gui.appliedenergistics2.Blank" ) )
			{
				// Clear the source info
				this.visSourceInfo.clearData();

				// Inform the user
				memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_CLEARED );

				// Mark dirty
				this.markDirty();
			}

			return true;
		}

		// Pass to super
		return super.onActivate( player, position );
	}

	@Override
	public void openInventory()
	{
		// Ignored
	}

	/**
	 * Loads part data from NBT tag
	 */
	@Override
	public void readFromNBT( final NBTTagCompound data )
	{
		// Call super
		super.readFromNBT( data );

		// Does the data tag have the list?
		if( data.hasKey( PartArcaneCraftingTerminal.INVENTORY_NBT_KEY ) )
		{
			// Get the list
			NBTTagList nbtTagList = (NBTTagList)data.getTag( PartArcaneCraftingTerminal.INVENTORY_NBT_KEY );

			for( int listIndex = 0; listIndex < nbtTagList.tagCount(); listIndex++ )
			{
				// Get the compound tag
				NBTTagCompound nbtCompound = nbtTagList.getCompoundTagAt( listIndex );

				// Get the slot list
				int slotIndex = nbtCompound.getByte( PartArcaneCraftingTerminal.SLOT_NBT_KEY );

				// Is it in range?
				if( this.isSlotInRange( slotIndex ) )
				{
					// Load the stack
					ItemStack slotStack = ItemStack.loadItemStackFromNBT( nbtCompound );

					// Is the slot the wand slot?
					if( slotIndex == PartArcaneCraftingTerminal.WAND_SLOT_INDEX )
					{
						// Validate the wand
						if( !ThEUtils.isItemValidWand( slotStack, false ) )
						{
							// Invalid wand data
							slotStack = null;
						}
					}

					// Set the slot
					this.slots[slotIndex] = slotStack;
				}
			}

		}

		// Sort order
		if( data.hasKey( PartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY ) )
		{
			this.sortingOrder = EnumCache.AE_SORT_ORDERS[data.getInteger( PartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY )];
		}

		// Sort direction
		if( data.hasKey( PartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY ) )
		{
			this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[data.getInteger( PartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY )];
		}

		// View mode
		if( data.hasKey( PartArcaneCraftingTerminal.VIEW_MODE_NBT_KEY ) )
		{
			this.viewMode = EnumCache.AE_VIEW_ITEMS[data.getInteger( PartArcaneCraftingTerminal.VIEW_MODE_NBT_KEY )];
		}

		// Vis source info
		if( data.hasKey( PartArcaneCraftingTerminal.VIS_INTERFACE_NBT_KEY ) )
		{
			this.visSourceInfo.readFromNBT( data, PartArcaneCraftingTerminal.VIS_INTERFACE_NBT_KEY );
		}
	}

	/**
	 * Adds a listener to the list if not already added.
	 * 
	 * @param container
	 */
	public void registerListener( final ContainerPartArcaneCraftingTerminal container )
	{
		// Is this already registered?
		if( !this.listeners.contains( container ) )
		{
			// Add to the list
			this.listeners.add( container );
		}

	}

	/**
	 * Removes a listener from the list.
	 * 
	 * @param container
	 */
	public void removeListener( final ContainerPartArcaneCraftingTerminal container )
	{
		// Remove the container from the listeners
		this.listeners.remove( container );
	}

	/**
	 * Renders the part while in the inventory
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventory( final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator ts = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderInventoryBox( renderer );

		helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
		ts.setColorOpaque_I( ThEPartBase.INVENTORY_OVERLAY_COLOR );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

		ts.setColorOpaque_I( AEColor.Black.mediumVariant );
		helper.renderInventoryFace( BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderInventoryBusLights( helper, renderer );
	}

	/**
	 * Renders the part in the world.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void renderStatic( final int x, final int y, final int z, final IPartRenderHelper helper, final RenderBlocks renderer )
	{
		Tessellator tessellator = Tessellator.instance;

		IIcon side = BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[3];

		// Main block
		helper.setTexture( side, side, side, side, side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		// Rotate
		this.rotateRenderer( renderer, false );

		// Face
		helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], ForgeDirection.SOUTH, renderer );

		if( this.isActive() )
		{
			// Set brightness
			tessellator.setBrightness( ThEPartBase.ACTIVE_FACE_BRIGHTNESS );

			// Draw corners
			helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
			tessellator.setColorOpaque_I( this.getHost().getColor().blackVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );
			tessellator.setColorOpaque_I( this.getHost().getColor().mediumVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );

			// Draw crafting overlay
			tessellator.setBrightness( 0xA000A0 );
			tessellator.setColorOpaque_I( AEColor.Lime.blackVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[4], ForgeDirection.SOUTH, renderer );

		}

		// Reset rotation
		this.rotateRenderer( renderer, true );

		// Cable lights
		helper.setBounds( 5.0F, 5.0F, 13.0F, 11.0F, 11.0F, 14.0F );
		this.renderStaticBusLights( x, y, z, helper, renderer );
	}

	/**
	 * Sets the contents of the specified inventory slot and
	 * updates the listeners.
	 * 
	 * @param slotIndex
	 * @param slotStack
	 * @return
	 */
	@Override
	public void setInventorySlotContents( final int slotIndex, final ItemStack slotStack )
	{
		if( this.setInventorySlotContentsWithoutNotify( slotIndex, slotStack ) )
		{
			// Inform the listeners
			this.notifyListeners( slotIndex );
		}
	}

	/**
	 * Sets the contents of the specified inventory slot without
	 * updating listeners.
	 * 
	 * @param slotIndex
	 * @param slotStack
	 * @return
	 */
	public boolean setInventorySlotContentsWithoutNotify( final int slotIndex, final ItemStack slotStack )
	{
		// Is the slot in range?
		if( this.isSlotInRange( slotIndex ) )
		{
			// Set the slot
			this.slots[slotIndex] = slotStack;

			return true;
		}

		return false;
	}

	/**
	 * Sets the sorting order and direction
	 * 
	 * @param order
	 * @param dir
	 */
	public void setSorts( final SortOrder order, final SortDir dir, final ViewItems viewMode )
	{
		this.sortingDirection = dir;

		this.sortingOrder = order;

		this.viewMode = viewMode;

		this.markDirty();
	}

	/**
	 * Swaps the armor stored in the ACT with what the specified player is
	 * wearing.
	 * 
	 * @param player
	 */
	public void swapStoredArmor( final EntityPlayer player )
	{

		for( int armorSlot = 0; armorSlot < 4; ++armorSlot )
		{
			// Get the stored armor
			ItemStack storedArmor = this.slots[PartArcaneCraftingTerminal.ARMOR_SLOT_MIN + armorSlot];

			// Get the player armor
			ItemStack playerArmor = player.inventory.armorInventory[3 - armorSlot];

			// Swap
			player.inventoryContainer.putStackInSlot( 5 + armorSlot, storedArmor );
			this.slots[PartArcaneCraftingTerminal.ARMOR_SLOT_MIN + armorSlot] = playerArmor;

		}

		player.inventoryContainer.detectAndSendChanges();

		// Mark for save
		this.markForSave();
	}

	/**
	 * Checks if the wand needs vis, and refills it if possible
	 */
	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Fast exit checks
		if( !this.visSourceInfo.hasSourceData() )
		{
			// No source data.
			return TickRateModulation.IDLE;
		}

		// Get the wand slot
		ItemStack stack = this.getStackInSlot( WAND_SLOT_INDEX );

		// Do we have a wand?
		if( ( stack == null ) )
		{
			// No wand
			return TickRateModulation.IDLE;
		}

		// Get the wand
		ItemWandCasting wand = (ItemWandCasting)stack.getItem();

		// Does the wand need vis?
		AspectList neededVis = wand.getAspectsWithRoom( stack );
		if( neededVis.size() <= 0 )
		{
			// Wand is charged
			return TickRateModulation.IDLE;
		}

		// Get the source
		IDigiVisSource visSource = this.visSourceInfo.tryGetSource( this.getGridBlock().getGrid() );

		// Did we get an active source?
		if( visSource == null )
		{
			// Invalid source
			return TickRateModulation.IDLE;
		}

		// Request vis for each aspect that the wand needs
		for( Aspect vis : neededVis.getAspects() )
		{
			// Calculate the size of the request
			int amountToDrain = wand.getMaxVis( stack ) - wand.getVis( stack, vis );

			// Request the vis
			int amountDrained = visSource.consumeVis( vis, amountToDrain );

			// Did we drain any?
			if( amountDrained > 0 )
			{
				// Add to the wand
				wand.addRealVis( stack, vis, amountDrained, true );

				// Inform listeners that wand has changed
				this.notifyListeners( PartArcaneCraftingTerminal.WAND_SLOT_INDEX );
			}

		}

		// Tick ASAP until the wand is charged.
		return TickRateModulation.URGENT;
	}

	/**
	 * Saves part data to NBT tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data, final PartItemStack saveType )
	{
		// Call super
		super.writeToNBT( data, saveType );

		// Only write NBT data if saving, or wrenched.
		if( ( saveType != PartItemStack.World ) && ( saveType != PartItemStack.Wrench ) )
		{
			return;
		}

		// Create a new tag list
		NBTTagList nbtList = new NBTTagList();

		// Loop over the slots
		for( int slotId = 0; slotId < PartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotId++ )
		{
			// Is the slot not null?
			if( this.slots[slotId] != null )
			{
				// Create the compound tag for this stack
				NBTTagCompound nbtCompound = new NBTTagCompound();

				// Set the slot ID
				nbtCompound.setByte( PartArcaneCraftingTerminal.SLOT_NBT_KEY, (byte)slotId );

				// Write the stack into the tag
				this.slots[slotId].writeToNBT( nbtCompound );

				// Append to the list
				nbtList.appendTag( nbtCompound );
			}
		}

		// Append the list to the data tag
		if( nbtList.tagCount() > 0 )
		{
			data.setTag( PartArcaneCraftingTerminal.INVENTORY_NBT_KEY, nbtList );
		}

		// Write direction
		if( this.sortingDirection != PartArcaneCraftingTerminal.DEFAULT_SORT_DIR )
		{
			data.setInteger( PartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY, this.sortingDirection.ordinal() );
		}

		// Write order
		if( this.sortingOrder != PartArcaneCraftingTerminal.DEFAULT_SORT_ORDER )
		{
			data.setInteger( PartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY, this.sortingOrder.ordinal() );
		}

		// Write view mode
		if( this.viewMode != PartArcaneCraftingTerminal.DEFAULT_VIEW_MODE )
		{
			data.setInteger( PartArcaneCraftingTerminal.VIEW_MODE_NBT_KEY, this.viewMode.ordinal() );
		}

		// Write the vis source info
		if( saveType == PartItemStack.World )
		{
			this.visSourceInfo.writeToNBT( data, PartArcaneCraftingTerminal.VIS_INTERFACE_NBT_KEY );
		}
	}
}
