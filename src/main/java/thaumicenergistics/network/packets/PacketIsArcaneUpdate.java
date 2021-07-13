package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.client.gui.part.GuiArcaneInscriber;

/**
 * @author Alex811
 */
public class PacketIsArcaneUpdate implements IMessage {

    public boolean isArcane;

    public PacketIsArcaneUpdate() {
    }

    public PacketIsArcaneUpdate(boolean isArcane) {
        this.isArcane = isArcane;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.isArcane = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isArcane);
    }

    public static class Handler implements IMessageHandler<PacketIsArcaneUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketIsArcaneUpdate message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiArcaneInscriber) {
                    GuiArcaneInscriber gui = (GuiArcaneInscriber) Minecraft.getMinecraft().currentScreen;
                    gui.setIsArcane(message.isArcane);
                }
            });
            return null;
        }
    }
}