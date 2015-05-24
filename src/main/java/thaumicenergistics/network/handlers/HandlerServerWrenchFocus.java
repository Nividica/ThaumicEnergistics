package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerWrenchFocus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerWrenchFocus
	implements IMessageHandler<PacketServerWrenchFocus, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerWrenchFocus message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
