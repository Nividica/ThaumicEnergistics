package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientPriority;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientPriority
	implements IMessageHandler<PacketClientPriority, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientPriority message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
