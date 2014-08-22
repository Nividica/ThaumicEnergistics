package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.server.PacketServerEssentiaStorageBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaStorageBus
	implements IMessageHandler<PacketServerEssentiaStorageBus, IMessage>
{

	@Override
	public IMessage onMessage( PacketServerEssentiaStorageBus message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
