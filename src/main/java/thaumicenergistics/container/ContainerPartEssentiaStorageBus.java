package thaumicenergistics.container;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
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
	private static int PLAYER_INV_POSITION_Y = 102;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = 160;
	
	private AEPartEssentiaStorageBus part;
	
	private EntityPlayer player;

	public ContainerPartEssentiaStorageBus( AEPartEssentiaStorageBus part, EntityPlayer player )
	{
		// Set the player
		this.player = player;
		
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
		
		// Register as a listener
		this.part.addListener( this );

	}
	
	@Override
	public void onContainerClosed(EntityPlayer player)
	{
		if( this.part != null )
		{
			this.part.removeListener( this );
		}
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
	
	public void setFilteredAspects( List<Aspect> filteredAspects )
	{
		new PacketClientAspectSlot().createFilterListUpdate( filteredAspects, this.player ).sendPacketToPlayer();
	}
}
