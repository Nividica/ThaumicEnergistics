package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientEssentiaCellTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaCellTerminal
	implements IMessageHandler<PacketClientEssentiaCellTerminal, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaCellTerminal message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
