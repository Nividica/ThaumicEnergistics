package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import thaumicenergistics.container.ContainerBase;
import thaumicenergistics.util.EssentiaFilter;

/**
 * @author BrockWS
 */
public class PacketEssentiaFilter implements IMessage {

    public EssentiaFilter essentiaFilter;

    public PacketEssentiaFilter() {
        this.essentiaFilter = new EssentiaFilter(0);
    }

    public PacketEssentiaFilter(EssentiaFilter essentiaFilter) {
        this.essentiaFilter = essentiaFilter;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Encode
        ByteBufUtils.writeTag(buf, this.essentiaFilter.serializeNBT());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Decode
        this.essentiaFilter.deserializeNBT(ByteBufUtils.readTag(buf));
    }

    public static class Handler implements IMessageHandler<PacketEssentiaFilter, IMessage> {

        @Override
        public IMessage onMessage(PacketEssentiaFilter message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (Minecraft.getMinecraft().player.openContainer instanceof ContainerBase) {
                    ContainerBase container = (ContainerBase) Minecraft.getMinecraft().player.openContainer;
                    if (container.getEssentiaFilter() != null)
                        container.setEssentiaFilter(message.essentiaFilter);
                }
            });
            return null;
        }
    }
}
