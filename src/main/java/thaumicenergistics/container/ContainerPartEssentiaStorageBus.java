package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.parts.AEPartEssentiaStorageBus;

public class ContainerPartEssentiaStorageBus
	extends ContainerWithNetworkTool
{
	/**
	 * X position offset for upgrade slots
	 */
	private static final int UPGRADE_SLOT_X = 187;

	/**
	 * Y position offset for upgrade slots
	 */
	private static final int UPGRADE_SLOT_Y = 8;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 140;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = 198;
	
	private AEPartEssentiaStorageBus part;

	public ContainerPartEssentiaStorageBus( AEPartEssentiaStorageBus part, EntityPlayer player )
	{
		// Set the part
		this.part = part;
		
		// Add the upgrade slot
		this.addUpgradeSlots( part.getUpgradeInventory(), 1, ContainerPartEssentiaStorageBus.UPGRADE_SLOT_X,
			ContainerPartEssentiaStorageBus.UPGRADE_SLOT_Y );

		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaStorageBus.PLAYER_INV_POSITION_Y,
			ContainerPartEssentiaStorageBus.HOTBAR_INV_POSITION_Y );

		// Bind to the network tool
		this.bindToNetworkTool( player.inventory, part.getHost().getLocation() );

	}

	/**
	 * Who can interact with the container?
	 */
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotNumber )
	{	
		// Get the slot
		Slot slot = this.getSlot( slotNumber );

		// Do we have a valid slot with an item?
		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			// Can this aspect be added to the filter list?
			if( ( this.part != null ) && ( this.part.addFilteredAspectFromItemstack( player, slot.getStack() ) ) )
			{
				return null;
			}

			// Pass to super
			return super.transferStackInSlot( player, slotNumber );
		}

		return null;
	}
}
