package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.PacketServerEssentiaTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaTerminal implements IMessageHandler<PacketServerEssentiaTerminal, IMessage>
{

	@Override
	public IMessage onMessage( PacketServerEssentiaTerminal message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
