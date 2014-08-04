package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.server.PacketServerArcaneCraftingTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerArcaneCraftingTerminal implements IMessageHandler<PacketServerArcaneCraftingTerminal, IMessage>
{

	@Override
	public IMessage onMessage( PacketServerArcaneCraftingTerminal message, MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
