package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerEssentiaCellWorkbench;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerEssentiaCellWorkbench
	implements IMessageHandler<PacketServerEssentiaCellWorkbench, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerEssentiaCellWorkbench message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}
}