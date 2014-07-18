package thaumicenergistics.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;

public class SlotNetworkTool
	extends Slot
{
	IInventory inventory;

	public SlotNetworkTool( INetworkTool inventory, int slotIndex, int xDisplayPosition, int yDisplayPosition )
	{
		super( inventory, slotIndex, xDisplayPosition, yDisplayPosition );

		this.inventory = inventory;
	}

	@Override
	public boolean isItemValid( ItemStack itemStack )
	{
		if ( itemStack != null )
		{
			Item item = itemStack.getItem();

			if ( item instanceof IUpgradeModule )
			{
				return ( (IUpgradeModule)item ).getType( itemStack ) != null;
			}
		}

		return false;
	}

}
