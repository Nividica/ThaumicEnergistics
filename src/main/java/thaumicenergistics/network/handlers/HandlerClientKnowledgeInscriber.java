package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientKnowledgeInscriber;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientKnowledgeInscriber
	implements IMessageHandler<PacketClientKnowledgeInscriber, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientKnowledgeInscriber message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
