package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.PacketEssentiaEmitter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerEssentiaLevelEmitter implements IMessageHandler<PacketEssentiaEmitter, IMessage>
{

	@Override
	public IMessage onMessage( PacketEssentiaEmitter message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
