package thaumicenergistics.util;

import net.minecraft.inventory.IInventory;

public interface IInventoryUpdateReceiver
{
	public void onInventoryChanged( IInventory sourceInventory );
}
