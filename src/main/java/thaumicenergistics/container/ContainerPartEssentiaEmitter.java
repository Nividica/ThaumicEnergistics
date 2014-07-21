package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Inventory container for essentia emitter.
 * 
 * @author Nividica
 * 
 */
public class ContainerPartEssentiaEmitter
	extends ContainerWithPlayerInventory
{
	/**
	 * X position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_X = 93;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 151;

	/**
	 * Slot ID offset the player inventory
	 */
	public static int PLAYER_INV_SLOT_OFFSET = 0;

	/**
	 * Create the container.
	 * 
	 * @param player
	 * The owner of this container.
	 */
	public ContainerPartEssentiaEmitter( EntityPlayer player )
	{
		// Get the players inventory and bind it to the container.
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaEmitter.PLAYER_INV_SLOT_OFFSET, ContainerPartEssentiaEmitter.PLAYER_INV_POSITION_X,
			ContainerPartEssentiaEmitter.PLAYER_INV_POSITION_Y );
	}

	/**
	 * Who can interact with the container?
	 */
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	// TODO: Fix this up, move to superclass
	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotNumber )
	{
		ItemStack itemStack = null;

		Slot slot = (Slot)this.inventorySlots.get( slotNumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemStack1 = slot.getStack();

			itemStack = itemStack1.copy();

			itemStack.stackSize = 1;

			if ( slotNumber == 0 )
			{
				( (Slot)this.inventorySlots.get( 0 ) ).putStack( null );
			}
			else if ( ( slotNumber >= 1 ) && ( slotNumber <= 36 ) )
			{
				( (Slot)this.inventorySlots.get( 0 ) ).putStack( itemStack );
			}
		}

		return null;
	}

}
