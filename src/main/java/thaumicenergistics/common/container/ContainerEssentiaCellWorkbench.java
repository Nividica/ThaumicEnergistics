package thaumicenergistics.common.container;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.container.slot.SlotRestrictive;
import thaumicenergistics.common.integration.tc.EssentiaItemContainerHelper;
import thaumicenergistics.common.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.common.tiles.TileEssentiaCellWorkbench;
import thaumicenergistics.common.utils.EffectiveSide;

public class ContainerEssentiaCellWorkbench
	extends ContainerWithPlayerInventory
{

	/**
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 169;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = PLAYER_INV_POSITION_Y + 58;

	/**
	 * Position of the cell slot.
	 */
	public static int CELL_SLOT_X = 152, CELL_SLOT_Y = 8;

	/**
	 * Cell Workbench.
	 */
	public final TileEssentiaCellWorkbench workbench;

	/**
	 * The cell slot.
	 */
	private final Slot cellSlot;

	public ContainerEssentiaCellWorkbench( final EntityPlayer player, final World world, final int x, final int y, final int z )
	{
		// Call super
		super( player );

		// Get the workbench
		this.workbench = (TileEssentiaCellWorkbench)world.getTileEntity( x, y, z );

		// Create the cell slot
		this.cellSlot = new SlotRestrictive( this.workbench, 0, ContainerEssentiaCellWorkbench.CELL_SLOT_X,
						ContainerEssentiaCellWorkbench.CELL_SLOT_Y );
		this.addSlotToContainer( this.cellSlot );

		// Bind to the player's inventory
		this.bindPlayerInventory( this.player.inventory, ContainerEssentiaCellWorkbench.PLAYER_INV_POSITION_Y,
			ContainerEssentiaCellWorkbench.HOTBAR_INV_POSITION_Y );

		// Register with the workbench
		if( EffectiveSide.isServerSide() )
		{
			this.workbench.registerListener( this );
		}
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.workbench != null )
		{
			return this.workbench.isUseableByPlayer( player );
		}

		return false;
	}

	/**
	 * Called when a client has requested the full list.
	 */
	public void onClientRequestPartitionList()
	{
		this.onPartitionChanged( this.workbench.getPartitionList() );
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		// Call super
		super.onContainerClosed( player );

		if( EffectiveSide.isServerSide() )
		{
			this.workbench.removeListener( this );
		}
	}

	/**
	 * Called when the partition list changes.
	 */
	public void onPartitionChanged( final ArrayList<Aspect> partitionList )
	{
		// Send to client
		Packet_C_AspectSlot.setFilterList( partitionList, this.player );
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Is this client side?
		if( EffectiveSide.isClientSide() )
		{
			// Do nothing.
			return null;
		}

		// Get the slot that was shift-clicked
		Slot slot = this.getSlotOrNull( slotNumber );

		// Is there a valid slot with and item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			boolean didMerge = false;

			// Get the itemstack in the slot
			ItemStack slotStack = slot.getStack();

			// Was the slot clicked the cell slot
			if( ( slot == this.cellSlot ) )
			{
				// Attempt to merge with the player inventory
				didMerge = this.mergeSlotWithPlayerInventory( slotStack );
			}
			// Was the slot clicked in the player or hotbar inventory?
			else if( this.slotClickedWasInPlayerInventory( slotNumber ) || this.slotClickedWasInHotbarInventory( slotNumber ) )
			{
				// Is the cell slot empty?
				if( !this.cellSlot.getHasStack() )
				{
					// Is the item in the clicked slot a valid cell?
					if( this.cellSlot.isItemValid( slotStack ) )
					{
						// Set the slot
						this.cellSlot.putStack( slotStack.copy() );

						// Clear the clicked slot
						slotStack.stackSize = 0;

						// Set that we merged
						didMerge = true;
					}
				}

				// Did we merge?
				if( !didMerge )
				{
					// Is the stack an aspect container?
					Aspect slotAspect = EssentiaItemContainerHelper.INSTANCE.getFilterAspectFromItem( slotStack );
					if( slotAspect != null )
					{
						// Attempt to add the aspect to the cell partition list
						didMerge = this.workbench.addAspectToPartition( slotAspect );
					}

					// Did we merge?
					if( !didMerge )
					{
						didMerge = this.swapSlotInventoryHotbar( slotNumber, slotStack );
					}
				}

			}

			if( didMerge )
			{
				// Did the merger drain the stack?
				if( slotStack.stackSize == 0 )
				{
					// Set the slot to have no item
					slot.putStack( null );
				}
				else
				{
					// Inform the slot its stack changed;
					slot.onSlotChanged();
				}

				// Send changes
				this.detectAndSendChanges();
			}
		}

		return null;
	}

}
