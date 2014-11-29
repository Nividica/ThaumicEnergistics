package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerEssentiaCellTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaCellTerminal
	implements IMessageHandler<PacketServerEssentiaCellTerminal, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerEssentiaCellTerminal message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
