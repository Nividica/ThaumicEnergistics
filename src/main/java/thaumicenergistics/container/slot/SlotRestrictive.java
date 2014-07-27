package thaumicenergistics.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Wrapper class to pass validation to inventory
 * @author Nividica
 *
 */
public class SlotRestrictive extends Slot
{
	/**
	 * The inventory we belong to
	 */
	private IInventory inventory;
	
	private int myIndex;

	/**
	 * Create the slot.
	 * @param inventory Inventory it belongs to.
	 * @param index Index of the slot
	 * @param x X position of the slot
	 * @param y Y position of the slot
	 */
	public SlotRestrictive(IInventory inventory, int index, int x, int y)
	{
		// Pass to super
		super( inventory, index, x, y );

		// Set the inventory
		this.inventory = inventory;
		
		// Set this index
		this.myIndex = index;
	}

	/**
	 * Pass the is valid call to the inventory.
	 */
	@Override
	public boolean isItemValid( ItemStack itemstack )
	{
		return this.inventory.isItemValidForSlot( this.myIndex, itemstack );
	}
}
