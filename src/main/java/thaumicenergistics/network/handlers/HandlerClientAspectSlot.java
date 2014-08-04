package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientAspectSlot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientAspectSlot
	implements IMessageHandler<PacketClientAspectSlot, IMessage>
{

	@Override
	public IMessage onMessage( PacketClientAspectSlot message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
