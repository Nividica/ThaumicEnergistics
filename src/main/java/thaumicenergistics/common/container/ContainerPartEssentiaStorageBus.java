package thaumicenergistics.common.container;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaStorageBus;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;

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

	/**
	 * The essentia storage bus.
	 */
	private final PartEssentiaStorageBus storageBus;

	/**
	 * Cache of filteredAspects.
	 */
	private final ArrayList<Aspect> filteredAspects = new ArrayList<Aspect>( PartEssentiaStorageBus.FILTER_SIZE );

	/**
	 * Cache of isVoidAllowed.
	 */
	private boolean isVoidAllowed = false;

	public ContainerPartEssentiaStorageBus( final PartEssentiaStorageBus part, final EntityPlayer player )
	{
		// Call super
		super( player );

		// Set the part
		this.storageBus = part;

		// Setup the filtered aspects
		for( int i = 0; i < PartEssentiaStorageBus.FILTER_SIZE; ++i )
		{
			this.filteredAspects.add( null );
		}

		// Add the upgrade slot
		this.addUpgradeSlots( part.getUpgradeInventory(), 1, ContainerPartEssentiaStorageBus.UPGRADE_SLOT_X,
			ContainerPartEssentiaStorageBus.UPGRADE_SLOT_Y );

		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaStorageBus.PLAYER_INV_POSITION_Y,
			ContainerPartEssentiaStorageBus.HOTBAR_INV_POSITION_Y );

		// Bind to the network tool
		this.bindToNetworkTool( player.inventory, part.getHost().getLocation(), 0, 0 );

	}

	@Override
	protected boolean detectAndSendChangesMP( final EntityPlayerMP playerMP )
	{
		// Has the filtered list changed?
		boolean updateFilters = false;
		for( int filterIndex = 0; filterIndex < PartEssentiaStorageBus.FILTER_SIZE; ++filterIndex )
		{
			if( this.filteredAspects.get( filterIndex ) != this.storageBus.getFilteredAspect( filterIndex ) )
			{
				// Found mismatch
				this.filteredAspects.set( filterIndex, this.storageBus.getFilteredAspect( filterIndex ) );
				updateFilters = true;
			}
		}
		if( updateFilters )
		{
			// Update the client
			Packet_C_AspectSlot.setFilterList( this.filteredAspects, this.player );
		}

		// Has the void mode changed?
		if( this.isVoidAllowed != this.storageBus.isVoidAllowed() )
		{
			// Update
			this.isVoidAllowed = this.storageBus.isVoidAllowed();
			Packet_C_EssentiaStorageBus.sendIsVoidAllowed( this.player, this.isVoidAllowed );
		}

		return false;
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.storageBus != null )
		{
			return this.storageBus.isPartUseableByPlayer( player );
		}
		return false;
	}

	public void setFilteredAspects( final List<Aspect> filteredAspects )
	{
		Packet_C_AspectSlot.setFilterList( filteredAspects, this.player );
	}

	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Get the slot
		Slot slot = this.getSlotOrNull( slotNumber );

		// Do we have a valid slot with an item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			// Can this aspect be added to the filter list?
			if( ( this.storageBus != null ) && ( this.storageBus.addFilteredAspectFromItemstack( player, slot.getStack() ) ) )
			{
				return null;
			}

			// Pass to super
			return super.transferStackInSlot( player, slotNumber );
		}

		return null;
	}
}
