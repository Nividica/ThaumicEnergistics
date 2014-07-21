package thaumicenergistics.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import thaumicenergistics.container.slot.SlotNetworkTool;
import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;

/**
 * Container that shows the network tool and player inventory.
 * @author Nividica
 *
 */
public abstract class ContainerWithNetworkTool
	extends ContainerWithPlayerInventory
{
	/**
	 * Number of rows in the network tool
	 */
	private static final int TOOL_ROWS = 3;

	/**
	 * Number of columns in the network tool
	 */
	private static final int TOOL_COLUMNS = 3;
	

	/**
	 * X position to start drawing slots
	 */
	private static final int SLOT_X_OFFSET = 187;

	/**
	 * Y position to start drawing slots
	 */
	private static final int SLOT_Y_OFFSET = 102;

	protected void bindToNetworkTool( InventoryPlayer playerInventory, DimensionalCoord partLocation )
	{
		// Check the player inventory for the network tool
		for( int slotIndex = 0; slotIndex < playerInventory.getSizeInventory(); slotIndex++ )
		{
			// Get the item in the current slot
			ItemStack stack = playerInventory.getStackInSlot( slotIndex );

			// Is it the network tool?
			if ( ( stack != null ) && ( stack.isItemEqual( AEApi.instance().items().itemNetworkTool.stack( 1 ) ) ) )
			{
				// Get the gui item for the tool
				IGuiItem guiItem = (IGuiItem)stack.getItem();

				// Get the gui for the tool
				INetworkTool networkTool = (INetworkTool)guiItem.getGuiObject( stack, partLocation.getWorld(), partLocation.x, partLocation.y,
					partLocation.z );

				// Add a slot for each network tool slot
				for( int column = 0; column < ContainerWithNetworkTool.TOOL_COLUMNS; column++ )
				{
					for( int row = 0; row < ContainerWithNetworkTool.TOOL_ROWS; row++ )
					{
						// Calculate the tools slot index
						int slotToolIndex = column + ( row * ContainerWithNetworkTool.TOOL_COLUMNS );

						this.addSlotToContainer( new SlotNetworkTool( networkTool, slotToolIndex, ContainerWithNetworkTool.SLOT_X_OFFSET +
										( column * ContainerWithPlayerInventory.SLOT_SIZE ), ( row * ContainerWithPlayerInventory.SLOT_SIZE ) +
										ContainerWithNetworkTool.SLOT_Y_OFFSET ) );
					}
				}
				
				// Done
				return;
			}
		}
	}
}
