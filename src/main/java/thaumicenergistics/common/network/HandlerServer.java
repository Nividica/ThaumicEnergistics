package thaumicenergistics.common.network;

import thaumicenergistics.common.network.packet.server.WrapperPacket_S;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Handles all server-side ThE packets.
 * 
 * @author Nividica
 * 
 */
public class HandlerServer
	implements IMessageHandler<WrapperPacket_S, IMessage>
{

	@Override
	public IMessage onMessage( final WrapperPacket_S message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}
}
