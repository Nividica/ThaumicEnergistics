package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.PacketClientEssentiaCell;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaCell implements IMessageHandler<PacketClientEssentiaCell, IMessage>
{

	@Override
	public IMessage onMessage( PacketClientEssentiaCell message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
