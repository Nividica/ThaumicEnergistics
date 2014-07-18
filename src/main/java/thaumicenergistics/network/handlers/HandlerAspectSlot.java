package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.PacketAspectSlot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerAspectSlot implements IMessageHandler<PacketAspectSlot, IMessage>
{

	@Override
	public IMessage onMessage( PacketAspectSlot message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
