package thaumicenergistics.container;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

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
	 * Binds the player inventory to this container.
	 * @param playerInventory Inventory to bind
	 * @param indexOffset The first player slot will take this id 
	 * @param inventoryOffsetY The Y position offset for the slots
	 * @param hotbarPositionY The Y position offset for hotbar slots
	 */
	protected void bindPlayerInventory( IInventory playerInventory, int indexOffset, int inventoryOffsetY, int hotbarPositionY )
	{

		// Main inventory
		for( int row = 0; row < ContainerWithPlayerInventory.ROWS; row++ )
		{
			for( int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++ )
			{
				this.addSlotToContainer( new Slot( playerInventory, indexOffset + ( column + ( row * ContainerWithPlayerInventory.COLUMNS ) ),
								ContainerWithPlayerInventory.INVENTORY_X_OFFSET + ( column * ContainerWithPlayerInventory.SLOT_SIZE ),
								( row * ContainerWithPlayerInventory.SLOT_SIZE ) + inventoryOffsetY ) );
			}
		}

		// Hot-bar
		for( int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++ )
		{
			this.addSlotToContainer( new Slot( playerInventory, column, ContainerWithPlayerInventory.INVENTORY_X_OFFSET +
							( column * ContainerWithPlayerInventory.SLOT_SIZE ), hotbarPositionY ) );
		}
	}

}
