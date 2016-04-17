package thaumicenergistics.api.storage;

import net.minecraft.inventory.IInventory;

/**
 * Implementer wants updates when an inventory is changed.
 *
 * @author Nividica
 *
 */
public interface IInventoryUpdateReceiver
{
	/**
	 * Called when an inventory is changed.
	 *
	 * @param sourceInventory
	 */
	public void onInventoryChanged( IInventory sourceInventory );
}
