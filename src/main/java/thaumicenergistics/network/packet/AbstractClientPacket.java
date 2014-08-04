package thaumicenergistics.network.packet;

import thaumicenergistics.network.ChannelHandler;

/**
 * Packet to be sent to the client.
 * @author Nividica
 *
 */
public abstract class AbstractClientPacket
	extends AbstractPacket
{

	/**
	 * Send this packet to all players.
	 */
	public void sendPacketToAllPlayers()
	{
		ChannelHandler.sendPacketToAllPlayers( this );
	}

	/**
	 * Send this packet to the player.
	 * 
	 * @param player
	 */
	public void sendPacketToPlayer()
	{
		ChannelHandler.sendPacketToPlayer( this, this.player );
	}
}
