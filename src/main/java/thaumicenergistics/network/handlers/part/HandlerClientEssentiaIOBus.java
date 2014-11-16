package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.client.PacketClientEssentiaIOBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaIOBus
	implements IMessageHandler<PacketClientEssentiaIOBus, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaIOBus message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
