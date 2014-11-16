package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientEssentiaCell;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaCell
	implements IMessageHandler<PacketClientEssentiaCell, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaCell message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
