package thaumicenergistics.api.storage;

import net.minecraft.inventory.IInventory;

public interface IInventoryUpdateReceiver
{
	public void onInventoryChanged( IInventory sourceInventory );
}
