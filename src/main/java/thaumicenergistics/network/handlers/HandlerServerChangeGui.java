package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerChangeGui;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerChangeGui
	implements IMessageHandler<PacketServerChangeGui, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerChangeGui message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}