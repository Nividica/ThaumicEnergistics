package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public abstract class ContainerWithPlayerInventory extends Container
{
	protected void bindPlayerInventory( IInventory playerInventory, int inventoryOffsetY, int hotbarPositionY )
	{
		// Main inventory
		for( int i = 0; i < 3; i++ )
		{
			for( int j = 0; j < 9; j++ )
			{
				this.addSlotToContainer( new Slot( playerInventory, j + ( i * 9 ) + 9, 8 + ( j * 18 ), ( i * 18 ) + inventoryOffsetY ) );
			}
		}

		// Hot-bar
		for( int i = 0; i < 9; i++ )
		{
			this.addSlotToContainer( new Slot( playerInventory, i, 8 + ( i * 18 ), hotbarPositionY ) );
		}
	}

	@Override
	public abstract boolean canInteractWith( EntityPlayer player );

}
