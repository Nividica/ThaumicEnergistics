package thaumicenergistics.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.common.parts.AEPartEssentiaLevelEmitter;

/**
 * Inventory container for essentia emitter.
 * 
 * @author Nividica
 * 
 */
public class ContainerPartEssentiaLevelEmitter
	extends ContainerWithPlayerInventory
{
	/**
	 * X position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 102;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = 160;

	/**
	 * The AE part associated with this container.
	 */
	private AEPartEssentiaLevelEmitter part;

	/**
	 * Create the container.
	 * 
	 * @param player
	 * The owner of this container.
	 */
	public ContainerPartEssentiaLevelEmitter( final AEPartEssentiaLevelEmitter part, final EntityPlayer player )
	{
		// Set the part
		this.part = part;

		// Get the players inventory and bind it to the container.
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaLevelEmitter.PLAYER_INV_POSITION_Y,
			ContainerPartEssentiaLevelEmitter.HOTBAR_INV_POSITION_Y );
	}

	/**
	 * Who can interact with the container?
	 */
	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Get the slot
		Slot slot = this.getSlot( slotNumber );

		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			// Can we set the filter based on this item?
			if( ( this.part != null ) && this.part.setFilteredAspectFromItemstack( player, slot.getStack() ) )
			{
				return null;
			}

			// Attempt to swap
			if( this.swapSlotInventoryHotbar( slotNumber, slot.getStack() ) )
			{
				// Did the merger drain the stack?
				if( slot.getStack().stackSize == 0 )
				{
					// Set the slot to have no item
					slot.putStack( null );
				}

				// Inform the slot its stack changed;
				slot.onSlotChanged();
			}
		}

		return null;

	}

}
