package thaumicenergistics.network.handlers;

import thaumicenergistics.network.packet.client.PacketClientEssentiaVibrationChamber;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerClientEssentiaVibrationChamber
	implements IMessageHandler<PacketClientEssentiaVibrationChamber, IMessage>
{

	@Override
	public IMessage onMessage( final PacketClientEssentiaVibrationChamber message, final MessageContext ctx )
	{
		message.execute();
		return null;
	}

}
