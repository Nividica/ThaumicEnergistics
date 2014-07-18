package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.PacketClientEssentiaTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaTerminal implements IMessageHandler<PacketClientEssentiaTerminal, IMessage>
{

	@Override
	public IMessage onMessage( PacketClientEssentiaTerminal message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
