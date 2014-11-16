package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.client.PacketClientEssentiaTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaTerminal
	implements IMessageHandler<PacketClientEssentiaTerminal, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaTerminal message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
