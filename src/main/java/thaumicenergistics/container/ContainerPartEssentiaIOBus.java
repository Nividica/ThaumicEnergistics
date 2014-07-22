package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.parts.AEPartEssentiaIO;

/**
 * Inventory container for the import and export busses.
 * 
 * @author Nividica
 * 
 */
public class ContainerPartEssentiaIOBus
	extends ContainerWithNetworkTool
{
	/**
	 * The number of upgrade slots we have
	 */
	private static int NUMBER_OF_UPGRADE_SLOTS = 4;

	/**
	 * The x position of the upgrade slots
	 */
	private static int UPGRADE_X_POS = 187;

	/**
	 * The Y position for the upgrade slots
	 */
	private static int UPGRADE_Y_POS = 8;

	/**
	 * X position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_X = 102;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 160;

	/**
	 * The part associated with this container
	 */
	private AEPartEssentiaIO part;
	
	/**
	 * Creates the container.
	 * 
	 * @param part
	 * The AE part associated with the container.
	 * @param player
	 * The owner of the container.
	 */
	public ContainerPartEssentiaIOBus( AEPartEssentiaIO part, EntityPlayer player )
	{
		// Set the part
		this.part = part;
		
		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaIOBus.PLAYER_INV_POSITION_X,
			ContainerPartEssentiaIOBus.PLAYER_INV_POSITION_Y );

		// Add the upgrade slots
		this.addUpgradeSlots( part.getUpgradeInventory(), ContainerPartEssentiaIOBus.NUMBER_OF_UPGRADE_SLOTS, ContainerPartEssentiaIOBus.UPGRADE_X_POS,
			ContainerPartEssentiaIOBus.UPGRADE_Y_POS );

		// Bind to the network tool
		this.bindToNetworkTool( player.inventory, part.getHost().getLocation() );
	}

	@Override
	protected void retrySlotClick( int par1, int par2, boolean par3, EntityPlayer player )
	{
		// Ignored
	}

	/**
	 * Who can interact with the container?
	 */
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	/**
	 * Called when the player shift+clicks on a slot.
	 */
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
