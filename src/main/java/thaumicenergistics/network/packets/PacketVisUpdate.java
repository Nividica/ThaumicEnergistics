package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import thaumicenergistics.client.gui.part.GuiArcaneTerminal;

/**
 * @author BrockWS
 */
public class PacketVisUpdate implements IMessage {

    public float vis;
    public float required;
    public float discount;

    public PacketVisUpdate() {
    }

    public PacketVisUpdate(float chunkVis, float requiredVis, float discount) {
        this.vis = chunkVis;
        this.required = requiredVis;
        this.discount = discount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.vis = buf.readFloat();
        this.required = buf.readFloat();
        this.discount = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.vis);
        buf.writeFloat(this.required);
        buf.writeFloat(this.discount);
    }

    public static class Handler implements IMessageHandler<PacketVisUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketVisUpdate message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiArcaneTerminal) {
                    GuiArcaneTerminal gui = (GuiArcaneTerminal) Minecraft.getMinecraft().currentScreen;
                    gui.setVisInfo(message.vis, message.required, message.discount);
                }
            });
            return null;
        }
    }
}