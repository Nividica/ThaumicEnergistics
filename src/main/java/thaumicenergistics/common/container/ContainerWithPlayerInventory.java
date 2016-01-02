package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerWithPlayerInventory
	extends Container
{
	/**
	 * The number of rows in the player inventory
	 */
	private static int ROWS = 3;

	/**
	 * The number of columns in the player inventory
	 */
	private static int COLUMNS = 9;

	/**
	 * The width and height of the slots
	 */
	protected static final int SLOT_SIZE = 18;

	/**
	 * X position offset for inventory slots
	 */
	private static final int INVENTORY_X_OFFSET = 8;

	/**
	 * Index of the first player slot
	 */
	private int firstPlayerSlotNumber = -1;

	/**
	 * Index of the last player slot
	 */
	private int lastPlayerSlotNumber = -1;

	/**
	 * Index of the first player slot
	 */
	private int firstHotbarSlotNumber = -1;

	/**
	 * Index of the last player slot
	 */
	private int lastHotbarSlotNumber = -1;

	/**
	 * Attempt to merge the specified slot stack with the hotbar inventory
	 * 
	 * @param slotStack
	 * @return
	 */
	protected final boolean mergeSlotWithHotbarInventory( final ItemStack slotStack )
	{
		return this.mergeItemStack( slotStack, this.firstHotbarSlotNumber, this.lastHotbarSlotNumber + 1, false );
	}

	/**
	 * Attempt to merge the specified slot stack with the player inventory
	 * 
	 * @param slotStack
	 * @return
	 */
	protected final boolean mergeSlotWithPlayerInventory( final ItemStack slotStack )
	{
		return this.mergeItemStack( slotStack, this.firstPlayerSlotNumber, this.lastPlayerSlotNumber + 1, false );
	}

	/**
	 * Checks if the slot clicked was in the hotbar inventory
	 * 
	 * @param slotNumber
	 * @return True if it was in the hotbar inventory, false otherwise.
	 */
	protected final boolean slotClickedWasInHotbarInventory( final int slotNumber )
	{
		return ( slotNumber >= this.firstHotbarSlotNumber ) && ( slotNumber <= this.lastHotbarSlotNumber );
	}

	/**
	 * Checks if the slot clicked was in the player inventory
	 * 
	 * @param slotNumber
	 * @return True if it was in the player inventory, false otherwise.
	 */
	protected final boolean slotClickedWasInPlayerInventory( final int slotNumber )
	{
		return ( slotNumber >= this.firstPlayerSlotNumber ) && ( slotNumber <= this.lastPlayerSlotNumber );
	}

	/**
	 * Attempt to move the item from hotbar <-> player inventory
	 * 
	 * @param slotNumber
	 * @return
	 */
	protected final boolean swapSlotInventoryHotbar( final int slotNumber, final ItemStack slotStack )
	{
		if( this.slotClickedWasInHotbarInventory( slotNumber ) )
		{
			return this.mergeSlotWithPlayerInventory( slotStack );
		}
		else if( this.slotClickedWasInPlayerInventory( slotNumber ) )
		{
			return this.mergeSlotWithHotbarInventory( slotStack );
		}

		return false;
	}

	/**
	 * Binds the player inventory to this container.
	 * 
	 * @param playerInventory
	 * Inventory to bind
	 * @param indexOffset
	 * The Y position offset for the slots
	 * @param hotbarPositionY
	 * The Y position offset for hotbar slots
	 */
	public final void bindPlayerInventory( final IInventory playerInventory, final int inventoryOffsetY, final int hotbarPositionY )
	{
		// TODO: Make the argument offsets on x an y.

		// Hot-bar ID's 0-8
		Slot hotbarSlot = null;
		for( int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++ )
		{
			// Create the slot
			hotbarSlot = new Slot( playerInventory, column, ContainerWithPlayerInventory.INVENTORY_X_OFFSET +
							( column * ContainerWithPlayerInventory.SLOT_SIZE ), hotbarPositionY );

			// Add the slot
			this.addSlotToContainer( hotbarSlot );

			// Check first
			if( column == 0 )
			{
				this.firstHotbarSlotNumber = hotbarSlot.slotNumber;
			}
		}

		// Set last
		if( hotbarSlot != null )
		{
			this.lastHotbarSlotNumber = hotbarSlot.slotNumber;
		}

		// Main inventory ID's 9-36
		Slot inventorySlot = null;
		for( int row = 0; row < ContainerWithPlayerInventory.ROWS; row++ )
		{
			for( int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++ )
			{
				// Create the slot
				inventorySlot = new Slot( playerInventory, ContainerWithPlayerInventory.COLUMNS +
								( column + ( row * ContainerWithPlayerInventory.COLUMNS ) ), ContainerWithPlayerInventory.INVENTORY_X_OFFSET +
								( column * ContainerWithPlayerInventory.SLOT_SIZE ), ( row * ContainerWithPlayerInventory.SLOT_SIZE ) +
								inventoryOffsetY );

				// Add the slot
				this.addSlotToContainer( inventorySlot );

				// Check first
				if( ( row + column ) == 0 )
				{
					this.firstPlayerSlotNumber = inventorySlot.slotNumber;
				}
			}
		}

		// Set last
		if( inventorySlot != null )
		{
			this.lastPlayerSlotNumber = inventorySlot.slotNumber;
		}
	}

	/**
	 * Gets all non-empty slot from the hotbar inventory.
	 * 
	 * @return
	 */
	public final List<Slot> getNonEmptySlotsFromHotbar()
	{
		List<Slot> hSlots = new ArrayList<Slot>();

		for( int slotNumber = this.firstHotbarSlotNumber; slotNumber <= this.lastHotbarSlotNumber; slotNumber++ )
		{
			// Get the slot
			Slot hSlot = this.getSlot( slotNumber );

			// Is the slot not-empty
			if( hSlot.getHasStack() )
			{
				// Add to the list
				hSlots.add( hSlot );
			}
		}

		return hSlots;
	}

	/**
	 * Gets all non-empty slot from the player inventory.
	 * 
	 * @return
	 */
	public final List<Slot> getNonEmptySlotsFromPlayerInventory()
	{
		List<Slot> pSlots = new ArrayList<Slot>();

		for( int slotNumber = this.firstPlayerSlotNumber; slotNumber <= this.lastPlayerSlotNumber; slotNumber++ )
		{
			// Get the slot
			Slot pSlot = this.getSlot( slotNumber );

			// Is the slot not-empty
			if( pSlot.getHasStack() )
			{
				// Add to the list
				pSlots.add( pSlot );
			}
		}

		return pSlots;
	}
}
