package thaumicenergistics.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumicenergistics.client.gui.block.GuiArcaneAssembler;
import thaumicenergistics.tile.TileArcaneAssembler;

import java.util.HashMap;

/**
 * @author Alex811
 */
public class PacketAssemblerGUIUpdate implements IMessage {

    public HashMap<String, Boolean> aspectExists = new HashMap<>();
    public boolean hasEnoughVis;

    public PacketAssemblerGUIUpdate() {
    }

    public PacketAssemblerGUIUpdate(TileArcaneAssembler TE) {
        this.aspectExists = TE.getAspectExists();
        this.hasEnoughVis = TE.getHasEnoughVis();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.hasEnoughVis = buf.readBoolean();
        int size = buf.readInt();
        for (int i = 0; i < size; i++)
            this.aspectExists.put(ByteBufUtils.readUTF8String(buf), buf.readBoolean());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.hasEnoughVis);
        buf.writeInt(this.aspectExists.size());
        this.aspectExists.forEach((k, v) -> {
            ByteBufUtils.writeUTF8String(buf, k);
            buf.writeBoolean(v);
        });
    }

    public static class Handler implements IMessageHandler<PacketAssemblerGUIUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketAssemblerGUIUpdate message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if(gui instanceof GuiArcaneAssembler)
                    ((GuiArcaneAssembler) gui).updateGUI(message.aspectExists, message.hasEnoughVis);
            });
            return null;
        }
    }
}