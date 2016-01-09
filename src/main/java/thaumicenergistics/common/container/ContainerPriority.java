package thaumicenergistics.common.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import thaumicenergistics.common.network.packet.client.Packet_C_Priority;
import thaumicenergistics.common.parts.ThEPartBase;
import thaumicenergistics.common.utils.ThEUtils;
import appeng.helpers.IPriorityHost;

public class ContainerPriority
	extends Container
{

	/**
	 * The host we are setting the priority for
	 */
	public final IPriorityHost host;

	/**
	 * Player associated with this container
	 */
	private final EntityPlayer player;

	public ContainerPriority( final IPriorityHost host, final EntityPlayer player )
	{
		// Set the host
		this.host = host;

		// Set the player
		this.player = player;
	}

	/**
	 * Sends the priority to the client
	 */
	private void sendPriorityToClient()
	{
		// Send the priority to the client
		Packet_C_Priority.sendPriority( this.host.getPriority(), this.player );
	}

	@Override
	public boolean canInteractWith( final EntityPlayer player )
	{
		if( this.host instanceof IInventory )
		{
			return ( (IInventory)this.host ).isUseableByPlayer( player );
		}
		else if( this.host instanceof ThEPartBase )
		{
			return ( (ThEPartBase)this.host ).isUseableByPlayer( player );
		}
		else if( this.host instanceof TileEntity )
		{
			return ThEUtils.canPlayerInteractWith( player, (TileEntity)this.host );
		}
		return false;
	}

	/**
	 * Called when a client requests to adjust the priority of the part.
	 * 
	 * @param newPriority
	 */
	public void onClientRequestAdjustPriority( final int adjustment )
	{
		// Adjust
		int newPriority = this.host.getPriority() + adjustment;

		// Set
		this.onClientRequestSetPriority( newPriority );
	}

	/**
	 * Called when a client requests the priority of the part.
	 */
	public void onClientRequestPriority()
	{
		// Send the priority
		this.sendPriorityToClient();
	}

	/**
	 * Called when a client requests to set the priority of the part.
	 * 
	 * @param newPriority
	 */
	public void onClientRequestSetPriority( final int newPriority )
	{
		// Set the priority
		this.host.setPriority( newPriority );

		// Send the reply
		this.sendPriorityToClient();
	}

}
