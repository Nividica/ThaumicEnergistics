package thaumicenergistics.network.packet;

import thaumicenergistics.network.ChannelHandler;

/**
 * Packet to be sent to the server.
 * 
 * @author Nividica
 * 
 */
public abstract class AbstractServerPacket
	extends AbstractPacket
{
	/**
	 * Send this packet to the server.
	 */
	public void sendPacketToServer()
	{
		ChannelHandler.sendPacketToServer( this );
	}
}
