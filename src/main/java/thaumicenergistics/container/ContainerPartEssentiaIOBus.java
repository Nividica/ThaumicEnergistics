package thaumicenergistics.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumicenergistics.container.slot.SlotRespective;
import thaumicenergistics.gui.GuiEssentiatIO;
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
	 * The starting Y position for the upgrade slots
	 */
	private static int UPGRADE_Y_OFFSET = 8;

	/**
	 * How far apart each slot should be drawn
	 */
	private static int UPGRADE_Y_MULTIPLIER = 18;

	/**
	 * X position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_X = 102;

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 160;

	/**
	 * Slot ID offset the player inventory
	 */
	public static int PLAYER_INV_SLOT_OFFSET = 9;

	private GuiEssentiatIO guiBusAspectIO;

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
		// Add the upgrade slots
		for( int slotIndex = 0; slotIndex < ContainerPartEssentiaIOBus.NUMBER_OF_UPGRADE_SLOTS; slotIndex++ )
		{
			this.addSlotToContainer( new SlotRespective( part.getUpgradeInventory(), slotIndex, ContainerPartEssentiaIOBus.UPGRADE_X_POS,
							ContainerPartEssentiaIOBus.UPGRADE_Y_OFFSET + ( slotIndex * ContainerPartEssentiaIOBus.UPGRADE_Y_MULTIPLIER ) ) );
		}

		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaIOBus.PLAYER_INV_SLOT_OFFSET,
			ContainerPartEssentiaIOBus.PLAYER_INV_POSITION_X, ContainerPartEssentiaIOBus.PLAYER_INV_POSITION_Y );

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
	 * Sets the gui associated with this container
	 * 
	 * @param gui
	 */
	@SideOnly(Side.CLIENT)
	public void setGui( GuiEssentiatIO gui )
	{
		this.guiBusAspectIO = gui;
	}

	// TODO: Fix this up, move to superclass
	@Override
	public ItemStack transferStackInSlot( EntityPlayer player, int slotId )
	{
		if ( ( this.guiBusAspectIO != null ) && ( this.guiBusAspectIO.shiftClick( this.getSlot( slotId ).getStack() ) ) )
		{
			return ( (Slot)this.inventorySlots.get( slotId ) ).getStack();
		}

		ItemStack itemStack = null;

		Slot slot = (Slot)this.inventorySlots.get( slotId );

		if ( ( slot != null ) && ( slot.getHasStack() ) )
		{
			ItemStack slotStack = slot.getStack();

			itemStack = slotStack.copy();

			if ( slotId < 36 )
			{
				if ( !this.mergeItemStack( slotStack, 36, this.inventorySlots.size(), true ) )
				{
					return null;
				}
			}
			else if ( !this.mergeItemStack( slotStack, 0, 36, false ) )
			{
				return slotStack;
			}

			if ( slotStack.stackSize == 0 )
			{
				slot.putStack( null );
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemStack;
	}

}
