package thaumicenergistics.parts;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumicenergistics.ThaumicEnergistics;
import thaumicenergistics.container.ContainerPartArcaneCraftingTerminal;
import thaumicenergistics.gui.GuiArcaneCraftingTerminal;
import thaumicenergistics.integration.tc.DigiVisSourceData;
import thaumicenergistics.integration.tc.IDigiVisSource;
import thaumicenergistics.registries.AEPartsEnum;
import thaumicenergistics.registries.EnumCache;
import thaumicenergistics.texture.BlockTextureManager;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEColor;
import appeng.items.storage.ItemViewCell;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEPartArcaneCraftingTerminal
	extends AbstractAEPartBase
	implements IInventory, IGridTickable
{
	/**
	 * Number of slots in the internal inventory
	 */
	private static final int MY_INVENTORY_SIZE = 16;

	/**
	 * Index of the wand, result, and view slot(s)
	 */
	public static final int WAND_SLOT_INDEX = 9, RESULT_SLOT_INDEX = 10, VIEW_SLOT_MIN = 11, VIEW_SLOT_MAX = 15;

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
	private final ItemStack[] slots = new ItemStack[AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE];

	/**
	 * Containers wishing to be notified on inventory changes
	 */
	private List<ContainerPartArcaneCraftingTerminal> listeners = new ArrayList<ContainerPartArcaneCraftingTerminal>();

	/**
	 * How the items are sorted.
	 */
	private SortOrder sortingOrder = SortOrder.NAME;

	/**
	 * What direction are the items sorted.
	 */
	private SortDir sortingDirection = SortDir.ASCENDING;

	/**
	 * Data pertaining to the linked digi-vis source
	 */
	private DigiVisSourceData visSourceInfo = new DigiVisSourceData();

	/**
	 * Creates the terminal
	 */
	public AEPartArcaneCraftingTerminal()
	{
		// Call super
		super( AEPartsEnum.ArcaneCraftingTerminal );
	}

	public static boolean isItemValidCraftingWand( final ItemStack stack )
	{
		// Ensure it is not null
		if( stack == null )
		{
			return false;
		}

		// Get the item
		Item item = stack.getItem();

		// Ensure the item is not null
		if( item == null )
		{
			return false;
		}

		// Ensure it is a casting wand
		if( !( item instanceof ItemWandCasting ) )
		{
			return false;
		}

		// Ensure it is not a staff
		if( ( (ItemWandCasting)item ).isStaff( stack ) )
		{
			return false;
		}

		// Valid wand
		return true;
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
		return( ( slotIndex >= 0 ) && ( slotIndex < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE ) );
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
		boolean notifyCrafting = ( slotIndex <= AEPartArcaneCraftingTerminal.RESULT_SLOT_INDEX );

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
	 * Checks if the specified player can open the gui.
	 */
	@Override
	protected boolean canPlayerOpenGui( final int playerID )
	{
		// Does the player have export & import permissions
		if( this.doesPlayerHaveSecurityClearance( playerID, SecurityPermissions.EXTRACT ) )
		{
			if( this.doesPlayerHaveSecurityClearance( playerID, SecurityPermissions.INJECT ) )
			{
				return true;
			}
		}

		return false;
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
	public void getBoxes( final IPartCollsionHelper helper )
	{
		helper.addBox( 2.0D, 2.0D, 14.0D, 14.0D, 14.0D, 16.0D );

		helper.addBox( 5.0D, 5.0D, 13.0D, 11.0D, 11.0D, 14.0D );
	}

	/**
	 * Gets the GUI associated with this part
	 */
	@Override
	public Object getClientGuiElement( final EntityPlayer player )
	{
		return new GuiArcaneCraftingTerminal( this, player );
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
		for( int slotIndex = 0; slotIndex < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotIndex++ )
		{
			// Skip if this is the output slot
			if( slotIndex == AEPartArcaneCraftingTerminal.RESULT_SLOT_INDEX )
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

	/**
	 * Determines how much power the part takes for just
	 * existing.
	 */
	@Override
	public double getIdlePowerUsage()
	{
		return AEPartArcaneCraftingTerminal.IDLE_POWER_DRAIN;
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
		return AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE;
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
		// We would like a tick ever 10 to 50 MC ticks
		return new TickingRequest( 10, 50, false, false );
	}

	/**
	 * Gets the world this inventory is in.
	 * 
	 * @return
	 */
	public World getWorldObj()
	{
		return this.hostTile.getWorldObj();
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
			if( slotIndex == AEPartArcaneCraftingTerminal.WAND_SLOT_INDEX )
			{
				// Is the item a wand?
				return AEPartArcaneCraftingTerminal.isItemValidCraftingWand( proposedStack );
			}
			// Is this a view slot?
			if( ( slotIndex >= AEPartArcaneCraftingTerminal.VIEW_SLOT_MIN ) && ( slotIndex <= AEPartArcaneCraftingTerminal.VIEW_SLOT_MAX ) )
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

	/**
	 * Who can use this?
	 */
	@Override
	public boolean isUseableByPlayer( final EntityPlayer player )
	{
		return true;
	}

	/**
	 * Ensures the inventory is saved.
	 */
	@Override
	public void markDirty()
	{
		// Mark the host tile
		this.hostTile.markDirty();
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

				// Inform the user
				memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );

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
		if( data.hasKey( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY ) )
		{
			// Get the list
			NBTTagList nbtTagList = (NBTTagList)data.getTag( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY );

			for( int listIndex = 0; listIndex < nbtTagList.tagCount(); listIndex++ )
			{
				// Get the compound tag
				NBTTagCompound nbtCompound = nbtTagList.getCompoundTagAt( listIndex );

				// Get the slot list
				int slotIndex = nbtCompound.getByte( AEPartArcaneCraftingTerminal.SLOT_NBT_KEY );

				// Is it in range?
				if( this.isSlotInRange( slotIndex ) )
				{
					// Set the slot
					this.slots[slotIndex] = ItemStack.loadItemStackFromNBT( nbtCompound );
				}
			}
		}

		// Sort order
		if( data.hasKey( AEPartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY ) )
		{
			this.sortingOrder = EnumCache.AE_SORT_ORDERS[data.getInteger( AEPartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY )];
		}

		// Sort direction
		if( data.hasKey( AEPartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY ) )
		{
			this.sortingDirection = EnumCache.AE_SORT_DIRECTIONS[data.getInteger( AEPartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY )];
		}

		// Vis source info
		this.visSourceInfo.readFromNBT( data, AEPartArcaneCraftingTerminal.VIS_INTERFACE_NBT_KEY );
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
		ts.setColorOpaque_I( AbstractAEPartBase.INVENTORY_OVERLAY_COLOR );
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

		helper.setTexture( side, side, side, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[0], side, side );
		helper.setBounds( 2.0F, 2.0F, 14.0F, 14.0F, 14.0F, 16.0F );
		helper.renderBlock( x, y, z, renderer );

		if( this.isActive() )
		{
			tessellator.setBrightness( AbstractAEPartBase.ACTIVE_BRIGHTNESS );

			helper.setBounds( 2.0F, 2.0F, 15.0F, 14.0F, 14.0F, 16.0F );
			tessellator.setColorOpaque_I( this.host.getColor().blackVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[2], ForgeDirection.SOUTH, renderer );

			tessellator.setColorOpaque_I( this.host.getColor().mediumVariant );
			helper.renderFace( x, y, z, BlockTextureManager.ARCANE_CRAFTING_TERMINAL.getTextures()[1], ForgeDirection.SOUTH, renderer );
		}

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
	public void setSorts( final SortOrder order, final SortDir dir )
	{
		this.sortingDirection = dir;

		this.sortingOrder = order;

		this.markDirty();
	}

	/**
	 * Checks if the wand needs vis, and refills it if possible
	 */
	@Override
	public TickRateModulation tickingRequest( final IGridNode node, final int TicksSinceLastCall )
	{
		// Fast exit checks
		if( ( this.gridBlock == null ) || ( !this.visSourceInfo.getHasData() ) )
		{
			// ACT does not have gridblock
			return TickRateModulation.IDLE;
		}

		// Get the wand slot
		ItemStack stack = this.getStackInSlot( WAND_SLOT_INDEX );

		// Do we have a wand?
		if( stack == null )
		{
			// Invalid wand
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
		IDigiVisSource visSource = this.visSourceInfo.tryGetSource( this.gridBlock.getGrid() );

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
				// Instead of calling this 4 times a second, I call it 2 times a second with an amount multiplier.
				// Think of it as simulated work. This greatly reduces server load.

				// Add to the wand
				wand.addRealVis( stack, vis, amountDrained * 10, true );
			}

		}

		// Tick ASAP until the wand is charged.
		return TickRateModulation.URGENT;
	}

	/**
	 * Saves part data to NBT tag
	 */
	@Override
	public void writeToNBT( final NBTTagCompound data )
	{
		// Call super
		super.writeToNBT( data );

		// Create a new tag list
		NBTTagList nbtList = new NBTTagList();

		// Loop over the slots
		for( int slotId = 0; slotId < AEPartArcaneCraftingTerminal.MY_INVENTORY_SIZE; slotId++ )
		{
			// Is the slot not null?
			if( this.slots[slotId] != null )
			{
				// Create the compound tag for this stack
				NBTTagCompound nbtCompound = new NBTTagCompound();

				// Set the slot ID
				nbtCompound.setByte( AEPartArcaneCraftingTerminal.SLOT_NBT_KEY, (byte)slotId );

				// Write the stack into the tag
				this.slots[slotId].writeToNBT( nbtCompound );

				// Append to the list
				nbtList.appendTag( nbtCompound );
			}
		}

		// Append the list to the data tag
		data.setTag( AEPartArcaneCraftingTerminal.INVENTORY_NBT_KEY, nbtList );

		// Write direction
		data.setInteger( AEPartArcaneCraftingTerminal.SORT_DIRECTION_NBT_KEY, this.sortingDirection.ordinal() );

		// Write order
		data.setInteger( AEPartArcaneCraftingTerminal.SORT_ORDER_NBT_KEY, this.sortingOrder.ordinal() );

		// Write the vis source info
		this.visSourceInfo.writeToNBT( data, AEPartArcaneCraftingTerminal.VIS_INTERFACE_NBT_KEY );
	}

}
