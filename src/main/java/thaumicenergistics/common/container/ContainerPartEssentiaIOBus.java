package thaumicenergistics.common.container;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.network.packet.client.Packet_C_AspectSlot;
import thaumicenergistics.common.network.packet.client.Packet_C_EssentiaIOBus;
import thaumicenergistics.common.parts.ThEPartEssentiaIOBus_Base;
import appeng.api.config.RedstoneMode;

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
	 * Y position for the player inventory
	 */
	private static int PLAYER_INV_POSITION_Y = 102;

	/**
	 * Y position for the hotbar inventory
	 */
	private static int HOTBAR_INV_POSITION_Y = 160;

	/**
	 * The part associated with this container
	 */
	private final ThEPartEssentiaIOBus_Base bus;

	/**
	 * Cached isVoidAllowed
	 */
	private boolean isVoidAllowed;

	/**
	 * Creates the container.
	 * 
	 * @param part
	 * The AE part associated with the container.
	 * @param player
	 * The owner of the container.
	 */
	public ContainerPartEssentiaIOBus( final ThEPartEssentiaIOBus_Base part, final EntityPlayer player )
	{
		// Call super
		super( player );

		// Set the part
		this.bus = part;

		// Bind to the player's inventory
		this.bindPlayerInventory( player.inventory, ContainerPartEssentiaIOBus.PLAYER_INV_POSITION_Y,
			ContainerPartEssentiaIOBus.HOTBAR_INV_POSITION_Y );

		// Add the upgrade slots
		this.addUpgradeSlots( part.getUpgradeInventory(), ContainerPartEssentiaIOBus.NUMBER_OF_UPGRADE_SLOTS,
			ContainerPartEssentiaIOBus.UPGRADE_X_POS, ContainerPartEssentiaIOBus.UPGRADE_Y_POS );

		// Bind to the network tool
		this.bindToNetworkTool( player.inventory, part.getHost().getLocation(), 0, 0 );

		// Register as a listener on the part
		this.bus.addListener( this );
	}

	@Override
	protected boolean detectAndSendChangesMP( final EntityPlayerMP playerMP )
	{
		// Has the void mode changed?
		if( this.isVoidAllowed != this.bus.isVoidAllowed() )
		{
			// Update
			this.isVoidAllowed = this.bus.isVoidAllowed();
			Packet_C_EssentiaIOBus.sendVoidMode( this.player, this.isVoidAllowed );
		}

		return false;
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.bus != null )
		{
			return this.bus.isPartUseableByPlayer( player );
		}
		return false;
	}

	@Override
	public void onContainerClosed( final EntityPlayer player )
	{
		if( this.bus != null )
		{
			this.bus.removeListener( this );
		}
	}

	public void setFilteredAspect( final List<Aspect> filteredAspects )
	{
		Packet_C_AspectSlot.setFilterList( filteredAspects, this.player );
	}

	public void setFilterSize( final byte filterSize )
	{
		Packet_C_EssentiaIOBus.sendFilterSize( this.player, filterSize );
	}

	public void setRedstoneControlled( final boolean isRedstoneControlled )
	{
		Packet_C_EssentiaIOBus.sendRedstoneControlled( this.player, isRedstoneControlled );
	}

	public void setRedstoneMode( final RedstoneMode redstoneMode )
	{
		Packet_C_EssentiaIOBus.sendRedstoneMode( this.player, redstoneMode );
	}

	/**
	 * Called when the player shift+clicks on a slot.
	 */
	@Override
	public ItemStack transferStackInSlot( final EntityPlayer player, final int slotNumber )
	{
		// Get the slot
		Slot slot = this.getSlotOrNull( slotNumber );

		// Do we have a valid slot with an item?
		if( ( slot != null ) && ( slot.getHasStack() ) )
		{
			// Can this aspect be added to the filter list?
			if( ( this.bus != null ) && ( this.bus.addFilteredAspectFromItemstack( player, slot.getStack() ) ) )
			{
				return null;
			}

			// Pass to super
			return super.transferStackInSlot( player, slotNumber );
		}

		return null;
	}

}
