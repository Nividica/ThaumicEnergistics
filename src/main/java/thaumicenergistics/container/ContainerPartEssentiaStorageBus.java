package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiEssentiaStorageBus;
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
	 * Slot ID offset the player inventory
	 */
	public static int PLAYER_INV_SLOT_OFFSET = 9;

	private GuiEssentiaStorageBus guiBusAspectStorage;

	public ContainerPartEssentiaStorageBus( AEPartEssentiaStorageBus part, EntityPlayer player )
	{
		// Add the upgrade slot
		this.addSlotToContainer( new SlotRespective( part.getUpgradeInventory(), 0, ContainerPartEssentiaStorageBus.UPGRADE_SLOT_X,
						ContainerPartEssentiaStorageBus.UPGRADE_SLOT_Y ) );

		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaStorageBus.PLAYER_INV_SLOT_OFFSET, 140, 198 );

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

	/**
	 * Set the gui associated with this container
	 * 
	 * @param guiBusAspectStorage
	 */
	public void setGui( GuiEssentiaStorageBus guiBusAspectStorage )
	{
		this.guiBusAspectStorage = guiBusAspectStorage;
	}

	// TODO: Fix and superclass
	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotnumber )
	{
		if ( this.guiBusAspectStorage != null )
		{
			this.guiBusAspectStorage.shiftClick( this.getSlot( slotnumber ).getStack() );
		}

		ItemStack itemstack = null;

		Slot slot = (Slot)this.inventorySlots.get( slotnumber );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack itemstack1 = slot.getStack();

			itemstack = itemstack1.copy();

			if ( slotnumber < 36 )
			{
				if ( !this.mergeItemStack( itemstack1, 36, this.inventorySlots.size(), true ) )
				{
					return null;
				}
			}
			else if ( !this.mergeItemStack( itemstack1, 0, 36, false ) )
			{
				return null;
			}
			if ( itemstack1.stackSize == 0 )
			{
				slot.putStack( null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

}
