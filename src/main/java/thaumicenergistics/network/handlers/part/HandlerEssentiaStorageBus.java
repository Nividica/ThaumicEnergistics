package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.PacketEssentiaStorageBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerEssentiaStorageBus implements IMessageHandler<PacketEssentiaStorageBus, IMessage>
{

	@Override
	public IMessage onMessage( PacketEssentiaStorageBus message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
