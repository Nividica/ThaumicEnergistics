package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.PacketEssentiaIOBus;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerEssentiaIOBus implements IMessageHandler<PacketEssentiaIOBus, IMessage>
{

	@Override
	public IMessage onMessage( PacketEssentiaIOBus message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
