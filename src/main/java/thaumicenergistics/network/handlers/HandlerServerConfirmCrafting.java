package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerConfirmCraftingJob;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerConfirmCrafting
	implements IMessageHandler<PacketServerConfirmCraftingJob, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerConfirmCraftingJob message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
