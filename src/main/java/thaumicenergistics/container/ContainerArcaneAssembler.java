package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumicenergistics.container.slot.SlotRestrictive;
import thaumicenergistics.items.ItemKnowledgeCore;
import thaumicenergistics.tileentities.TileArcaneAssembler;
import thaumicenergistics.util.EffectiveSide;
import appeng.container.slot.SlotInaccessible;

public class ContainerArcaneAssembler
	extends ContainerWithPlayerInventory
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
	 * Reference to the arcane assembler
	 */
	public TileArcaneAssembler assembler;

	private SlotRestrictive kCoreSlot;

	public ContainerArcaneAssembler( final EntityPlayer player, final World world, final int X, final int Y, final int Z )
	{
		// Bind to the players inventory
		this.bindPlayerInventory( player.inventory, ContainerArcaneAssembler.PLAYER_INV_POSITION_Y, ContainerArcaneAssembler.HOTBAR_INV_POSITION_Y );

		// Get the assembler
		this.assembler = (TileArcaneAssembler)world.getTileEntity( X, Y, Z );

		// Get the assemblers inventory
		IInventory asmInv = this.assembler.getInternalInventory();

		// Add the kcore slot
		this.kCoreSlot = new SlotRestrictive( asmInv, TileArcaneAssembler.KCORE_SLOT_INDEX, ContainerArcaneAssembler.KCORE_SLOT_X,
						ContainerArcaneAssembler.KCORE_SLOT_Y );
		this.addSlotToContainer( this.kCoreSlot );

		// Create the slots
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
				// Attempt to merge with kcore slot
				if( slotStack.getItem() instanceof ItemKnowledgeCore )
				{
					didMerge = this.mergeItemStack( slotStack, this.kCoreSlot.slotNumber, this.kCoreSlot.slotNumber + 1, false );
				}

				// Was the stack merged?
				if( !didMerge )
				{
					// Attempt to merge with player inventory
					didMerge = this.swapSlotInventoryHotbar( slotNumber, slotStack );
				}
			}
			// Was the slot clicked the KCore slot?
			else if( this.kCoreSlot.slotNumber == slotNumber )
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
			}
		}

		// All done.
		return null;
	}

}
