package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPartEssentiaEmitter extends ContainerWithPlayerInventory
{
	public ContainerPartEssentiaEmitter(EntityPlayer player)
	{
		this.bindPlayerInventory( player.inventory, 93, 151 );
	}

	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotNumber )
	{
		ItemStack itemStack = null;

		Slot slot = (Slot) this.inventorySlots.get( slotNumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemStack1 = slot.getStack();

			itemStack = itemStack1.copy();

			itemStack.stackSize = 1;

			if ( slotNumber == 0 )
			{
				( (Slot) this.inventorySlots.get( 0 ) ).putStack( null );
			}
			else if ( ( slotNumber >= 1 ) && ( slotNumber <= 36 ) )
			{
				( (Slot) this.inventorySlots.get( 0 ) ).putStack( itemStack );
			}
		}

		return null;
	}

}
