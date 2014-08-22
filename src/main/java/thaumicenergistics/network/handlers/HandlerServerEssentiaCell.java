package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerEssentiaCell;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaCell
	implements IMessageHandler<PacketServerEssentiaCell, IMessage>
{

	@Override
	public IMessage onMessage( PacketServerEssentiaCell message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
