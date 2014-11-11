package thaumicenergistics.network.handlers.part;

import thaumicenergistics.network.packet.client.PacketClientArcaneCraftingTerminal;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientArcaneCraftingTerminal
	implements IMessageHandler<PacketClientArcaneCraftingTerminal, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientArcaneCraftingTerminal message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
