package thaumicenergistics.network;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import appeng.api.storage.data.IItemList;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.part.GuiEssentiaTerminal;
import thaumicenergistics.integration.appeng.AEEssentiaStack;
import thaumicenergistics.integration.appeng.EssentiaList;

/**
 * @author BrockWS
 */
public class PacketMEEssentiaUpdate implements IMessage {

    public IItemList<IAEEssentiaStack> list;

    public PacketMEEssentiaUpdate() {
        this.list = new EssentiaList();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        while (buf.isReadable()) {
            this.list.add(AEEssentiaStack.fromPacket(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (IAEEssentiaStack stack : this.list) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void appendStack(IAEEssentiaStack stack) {
        this.list.add(stack);
    }

    public static class Handler implements IMessageHandler<PacketMEEssentiaUpdate, IMessage> {

        @Override
        public IMessage onMessage(PacketMEEssentiaUpdate message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiEssentiaTerminal) {
                    GuiEssentiaTerminal gui = (GuiEssentiaTerminal) Minecraft.getMinecraft().currentScreen;
                    gui.onMEStorageUpdate(message.list);
                }
            });
            return null;
        }
    }
}
