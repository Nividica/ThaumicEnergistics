package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.server.PacketServerKnowledgeInscriber;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerServerKnowledgeInscriber
	implements IMessageHandler<PacketServerKnowledgeInscriber, IMessage>
{

	@Override
	public IMessage onMessage( final PacketServerKnowledgeInscriber message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
