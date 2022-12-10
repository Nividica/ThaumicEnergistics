package thaumicenergistics.common.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.common.network.packet.client.WrapperPacket_C;

/**
 * Handles all client-side ThE packets.
 *
 * @author Nividica
 *
 */
public class HandlerClient implements IMessageHandler<WrapperPacket_C, IMessage> {

    @Override
    public IMessage onMessage(final WrapperPacket_C message, final MessageContext ctx) {
        message.execute();
        return null;
    }
}
