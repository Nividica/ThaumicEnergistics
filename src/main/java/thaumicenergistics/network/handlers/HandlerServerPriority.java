package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerPriority;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerPriority
	implements IMessageHandler<PacketServerPriority, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerPriority message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
