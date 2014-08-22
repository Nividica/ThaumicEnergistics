package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.server.PacketServerEssentiaEmitter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaLevelEmitter
	implements IMessageHandler<PacketServerEssentiaEmitter, IMessage>
{

	@Override
	public IMessage onMessage( PacketServerEssentiaEmitter message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
