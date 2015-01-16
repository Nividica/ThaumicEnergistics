package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketAreaParticleFX;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerAreaParticleFX
	implements IMessageHandler<PacketAreaParticleFX, IMessage>
{

	@Override
	public IMessage onMessage( final PacketAreaParticleFX message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
