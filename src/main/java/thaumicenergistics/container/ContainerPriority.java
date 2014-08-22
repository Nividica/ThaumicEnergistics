package thaumicenergistics.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import thaumicenergistics.network.packet.client.PacketClientPriority;
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

	public ContainerPriority( IPriorityHost host, EntityPlayer player )
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
		new PacketClientPriority().createSendPriority( this.host.getPriority(), this.player ).sendPacketToPlayer();
	}

	/**
	 * Who can interact with this?
	 */
	@Override
	public boolean canInteractWith( EntityPlayer player )
	{
		return true;
	}

	/**
	 * Called when a client requests to adjust the priority of the part.
	 * 
	 * @param newPriority
	 */
	public void onClientRequestAdjustPriority( int adjustment )
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
	public void onClientRequestSetPriority( int newPriority )
	{
		// Set the priority
		this.host.setPriority( newPriority );

		// Send the reply
		this.sendPriorityToClient();
	}

}
