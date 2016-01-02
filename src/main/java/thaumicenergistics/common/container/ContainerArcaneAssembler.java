package thaumicenergistics.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.IVisDiscountGear;
import thaumicenergistics.common.blocks.BlockArcaneAssembler;
import thaumicenergistics.common.container.slot.SlotArmor;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.items.ItemKnowledgeCore;
import thaumicenergistics.common.tiles.TileArcaneAssembler;
import thaumicenergistics.common.utils.EffectiveSide;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.container.slot.SlotInaccessible;

public class ContainerArcaneAssembler
	extends ContainerWithNetworkTool
{

	/**
	 * Y position for the player inventory.
	 */
	private static final int PLAYER_INV_POSITION_Y = 115;

	/**
	 * Y position for the hotbar inventory.
	 */
	private static final int HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Starting position of the pattern slots.
	 */
	private static final int PATTERN_SLOT_X = 26, PATTERN_SLOT_Y = 25;

	/**
	 * Number of rows and columns of the pattern slots.
	 */
	private static final int PATTERN_ROWS = 3, PATTERN_COLS = 7;

	/**
	 * Position of the kcore slot.
	 */
	private static final int KCORE_SLOT_X = 187, KCORE_SLOT_Y = 8;

	/**
	 * Upgrade slots.
	 */
	private static final int UPGRADE_SLOT_COUNT = BlockArcaneAssembler.MAX_SPEED_UPGRADES, UPGRADE_SLOT_X = 187, UPGRADE_SLOT_Y = 26;

	/**
	 * Target slot.
	 */
	private static final int TARGET_SLOT_X = 14, TARGET_SLOT_Y = 87;

	/**
	 * Discount armor slots.
	 */
	private static final int DISCOUNT_ARMOR_SLOT_X = 210, DISCOUNT_ARMOR_SLOT_Y = 26, DISCOUNT_ARMOR_COUNT = 4;

	/**
	 * Reference to the arcane assembler
	 */
	public TileArcaneAssembler assembler;

	/**
	 * Knowledge Core slot.
	 */
	private SlotRestrictive kCoreSlot;

	/**
	 * Discount armor slots.
	 */
	private SlotArmor[] discountSlots = new SlotArmor[4];

	public ContainerArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Get the assembler
		this.assembler = (TileArcaneAssembler)world.getTileEntity( X, Y, Z );

		// Get the assemblers inventory
		IInventory asmInv = this.assembler.getInternalInventory();

		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerArcaneAssembler.PLAYER_INV_POSITION_Y, ContainerArcaneAssembler.HOTBAR_INV_POSITION_Y );

		// Bind to network tool
		this.bindToNetworkTool( player.inventory, this.assembler.getLocation(), 0, 35 );

		// Add the kcore slot
		this.kCoreSlot = new SlotRestrictive( asmInv, TileArcaneAssembler.KCORE_SLOT_INDEX, ContainerArcaneAssembler.KCORE_SLOT_X,
						ContainerArcaneAssembler.KCORE_SLOT_Y );
		this.addSlotToContainer( this.kCoreSlot );

		// Create the pattern slots
		for( int row = 0; row < ContainerArcaneAssembler.PATTERN_ROWS; row++ )
		{
			for( int col = 0; col < ContainerArcaneAssembler.PATTERN_COLS; col++ )
			{
				// Calculate the index
				int index = TileArcaneAssembler.PATTERN_SLOT_INDEX + ( ( row * ContainerArcaneAssembler.PATTERN_COLS ) + col );

				// Add the slot
				this.addSlotToContainer( new SlotInaccessible( asmInv, index, ContainerArcaneAssembler.PATTERN_SLOT_X + ( 18 * col ),
								ContainerArcaneAssembler.PATTERN_SLOT_Y + ( 18 * row ) ) );
			}
		}

		// Create the upgrade slots
		this.addUpgradeSlots( this.assembler.getUpgradeInventory(), ContainerArcaneAssembler.UPGRADE_SLOT_COUNT,
			ContainerArcaneAssembler.UPGRADE_SLOT_X, ContainerArcaneAssembler.UPGRADE_SLOT_Y );

		// Create the target slot
		this.addSlotToContainer( new SlotInaccessible( asmInv, TileArcaneAssembler.TARGET_SLOT_INDEX, ContainerArcaneAssembler.TARGET_SLOT_X,
						ContainerArcaneAssembler.TARGET_SLOT_Y ) );

		// Add armor slots
		for( int index = 0; index < ContainerArcaneAssembler.DISCOUNT_ARMOR_COUNT; index++ )
		{
			this.discountSlots[index] = new SlotArmor( asmInv, TileArcaneAssembler.DISCOUNT_ARMOR_INDEX + index,
							ContainerArcaneAssembler.DISCOUNT_ARMOR_SLOT_X, ContainerArcaneAssembler.DISCOUNT_ARMOR_SLOT_Y + ( index * 18 ), index,
							true );

			this.addSlotToContainer( this.discountSlots[index] );
		}

	}

	/**
	 * Attempts to merge the itemstack with the armor slots.
	 * 
	 * @param slotStack
	 * @return
	 */
	private boolean mergeWithArmorSlots( final ItemStack slotStack )
	{
		// Check each armor slot
		for( int index = 0; index < ContainerArcaneAssembler.DISCOUNT_ARMOR_COUNT; index++ )
		{
			// Is the item for the slot?
			if( this.discountSlots[index].isItemValid( slotStack ) )
			{
				// Is the slot empty?
				if( !this.discountSlots[index].getHasStack() )
				{
					// Place the item
					this.discountSlots[index].putStack( slotStack.copy() );

					// Clear the input stack
					slotStack.stackSize = 0;

					return true;
				}
				break;
			}
		}

		return false;
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return true;
	}

	/**
	 * Called when the player shift+clicks on a slot
	 */
	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			// Do nothing.
			return null;
		}

		// Get the slot that was shift-clicked
		Slot slot = (Slot)this.inventorySlots.get( slotNumber );

		// Is there a valid slot with and item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			boolean didMerge = false;

			// Get the itemstack in the slot
			ItemStack slotStack = slot.getStack();

			// Was the slot clicked in the player or hotbar inventory?
			if( this.slotClickedWasInPlayerInventory( slotNumber ) || this.slotClickedWasInHotbarInventory( slotNumber ) )
			{
				// Skip upgrade cards, let the super class handle them
				if( !( slotStack.getItem() instanceof IUpgradeModule ) )
				{
					// Attempt to merge with kcore slot
					if( slotStack.getItem() instanceof ItemKnowledgeCore )
					{
						didMerge = this.mergeItemStack( slotStack, this.kCoreSlot.slotNumber, this.kCoreSlot.slotNumber + 1, false );
					}
					else if( slotStack.getItem() instanceof IVisDiscountGear )
					{
						didMerge = this.mergeWithArmorSlots( slotStack );
					}

					// Was the stack merged?
					if( !didMerge )
					{
						// Attempt to merge with player inventory
						didMerge = this.swapSlotInventoryHotbar( slotNumber, slotStack );
					}
				}
			}
			// Was the slot clicked the KCore slot or armor slots?
			else if( ( this.kCoreSlot.slotNumber == slotNumber ) ||
							( ( slotNumber >= this.discountSlots[0].slotNumber ) && ( slotNumber <= this.discountSlots[ContainerArcaneAssembler.DISCOUNT_ARMOR_COUNT - 1].slotNumber ) ) )
			{
				// Attempt to merge with player hotbar
				didMerge = this.mergeSlotWithHotbarInventory( slotStack );

				// Was the stack merged?
				if( !didMerge )
				{
					// Attempt to merge with the player inventory
					didMerge = this.mergeSlotWithPlayerInventory( slotStack );
				}
			}

			// Was the stack merged?
			if( didMerge )
			{

				// Did the merger drain the stack?
				if( ( slotStack == null ) || ( slotStack.stackSize == 0 ) )
				{
					// Set the slot to have no item
					slot.putStack( null );
				}
				else
				{
					// Inform the slot its stack changed;
					slot.onSlotChanged();
				}

				// Send changes
				this.detectAndSendChanges();

				return null;
			}
		}

		// Call super
		return super.transferStackInSlot( player, slotNumber );
	}
}
