package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.client.PacketClientEssentiaEmitter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaLevelEmitter
	implements IMessageHandler<PacketClientEssentiaEmitter, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaEmitter message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
