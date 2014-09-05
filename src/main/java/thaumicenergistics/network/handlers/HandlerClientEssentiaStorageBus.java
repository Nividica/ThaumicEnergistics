package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientEssentiaStorageBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaStorageBus
implements IMessageHandler<PacketClientEssentiaStorageBus, IMessage>
{

	@Override
	public IMessage onMessage( PacketClientEssentiaStorageBus message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
