package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerAspectSlot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerAspectSlot
	implements IMessageHandler<PacketServerAspectSlot, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerAspectSlot message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
