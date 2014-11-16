package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.server.PacketServerEssentiaTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaTerminal
	implements IMessageHandler<PacketServerEssentiaTerminal, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerEssentiaTerminal message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
